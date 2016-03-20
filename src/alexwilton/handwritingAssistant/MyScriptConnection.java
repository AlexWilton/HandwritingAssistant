package alexwilton.handwritingAssistant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.myscript.cloud.sample.ws.api.Point;
import com.myscript.cloud.sample.ws.api.Stroke;
import com.myscript.cloud.sample.ws.api.text.TextInput;
import com.myscript.cloud.sample.ws.api.text.TextOutput;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.apache.commons.codec.binary.Hex;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.*;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

@ClientEndpoint
public class MyScriptConnection {
    private MessageHandler messageHandler;
    private Session userSession = null;
    private ObjectMapper mapper = new ObjectMapper();
    private String applicationKey, hmacKey, myScriptURL;
    private String currentInstanceId = null;

    enum ConnectionStatus { DISCONNECTED, READY_TO_START, STARTED}
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    public MyScriptConnection(String applicationKey, String hmacKey, String myScriptURL){
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.myScriptURL = myScriptURL;
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    private void connect(){
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create(myScriptURL));
            JSONObject init1 = new JSONObject();
            init1.put("type", "applicationKey");
            init1.put("applicationKey", applicationKey);
            sendMessage(init1.toJSONString());
        } catch (Exception e) {
            System.err.println("System failed to connected to MyScript");
            throw new RuntimeException(e);
        }
    }
    @OnOpen
    public void onOpen(Session userSession) {this.userSession = userSession;}
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        status = ConnectionStatus.DISCONNECTED;
    }
    @OnMessage
    public void onMessage(String jsonMsg) {
        JSONObject msg = (JSONObject) JSONValue.parse(jsonMsg);
        switch ((String) msg.get("type")){
            case "hmacChallenge": //compute response and send it
                String challenge = (String) msg.get("challenge");
                String hmac = computeHMAC(applicationKey, hmacKey, challenge);
                JSONObject init2 = new JSONObject();
                init2.put("type", "hmac");
                init2.put("applicationKey", applicationKey);
                init2.put("challenge", challenge);
                init2.put("hmac", hmac);
                sendMessage(init2.toJSONString());
                break;
            case "init":
            case "reset":
                status = ConnectionStatus.READY_TO_START;
                break;
            case "textResult":
                TextOutput textOutput = getTextOutputs(jsonMsg);
                currentInstanceId = textOutput.getInstanceId();
                if(messageHandler != null) {
                    String textRecognised = getTextOutputResult(jsonMsg);
                    messageHandler.handleMessage(textRecognised);
                }else
                    System.out.println("Result received: " + getTextOutputResult(jsonMsg));
                sendResetRequest();
                break;
        }
    }

    private void sendResetRequest() {
        JSONObject reset = new JSONObject();
        reset.put("type", "reset");
        sendMessage(reset.toJSONString());
        currentInstanceId = null;
    }

    private void sendStartRecogRequest(Stroke[] strokes){
        waitForReady();
        JSONObject start1 = getTextInput(strokes);
        start1.put("type", "start");
        JSONObject textParam = new JSONObject();
        textParam.put("language", "en_GB");
        textParam.put("textInputMode", "CURSIVE");
        textParam.put("textProperties", new JSONObject());
        start1.put("textParameter", textParam);
        status = ConnectionStatus.STARTED;
        sendMessage(start1.toJSONString());

    }

    private void sendContinueRecogRequest(Stroke[] strokes){
        waitForReady();
        JSONObject start1 = getTextInput(strokes);
        start1.put("type", "continue");
        start1.put("instanceId", currentInstanceId);
        sendMessage(start1.toJSONString());

    }


    private void waitForReady(){
        switch (status){
            case DISCONNECTED: connect();
            case STARTED:
                while(status != ConnectionStatus.READY_TO_START){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case READY_TO_START: break;
        }
    }

    protected String getTextOutputResult(String json) {
        TextOutput output = getTextOutputs(json);
        final int selectedCandidateIdx = output.getResult().getTextSegmentResult().getSelectedCandidateIdx();
        return output.getResult().getTextSegmentResult().getCandidates().get(selectedCandidateIdx).getLabel();
    }

    private TextOutput getTextOutputs(String json) {
        Reader jsonReader = new StringReader(json);
        TextOutput output = null;
        try {
            output = mapper.readValue(jsonReader, TextOutput.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    private JSONObject getTextInput(Stroke[] strokes) {
        TextInput input = new TextInput();

        for (int strokeIndex = 0; strokeIndex < strokes.length; strokeIndex++) {
            input.addComponent();
            for (final Point point : strokes[strokeIndex].getPoints())
                input.addComponentPoint(point.x, point.y);
        }

        Writer jsonWriter = new StringWriter();
        try {
           mapper.writeValue(jsonWriter, input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (JSONObject) JSONValue.parse(jsonWriter.toString());
    }


    private void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Recognise strokes and set message handler for handing recognition result.
     * Method waits until connection is free before setting handler and sending request (synchronous).
     * @param strokeArray Strokes to analyse
     * @param messageHandler Method to deal with response.
     */
    public void recognizeStrokes(Stroke[] strokeArray, final MessageHandler messageHandler) {
        //wait for connection to be free
        while(status == ConnectionStatus.STARTED){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //set message handler then send message
        this.messageHandler = messageHandler;
        sendStartRecogRequest(strokeArray);
    }

    /**
     * Compute required value to return to the server with hmac SHA512 hash algorithm
     * It prevents from man-in-the-middle key theft.
     * This method is provided by MyScript
     *
     * @applicationKey : applicationKey
     * @hmackey :hmackey
     * @jsonInput: textInput, mathInput, shapeInput, musicInput or AnalyzerInput
     *
     */
    private String computeHMAC(String applicationKey, String hmacKey, String jsonInput) {
        final String HMAC_SHA_512_ALGORITHM = "HmacSHA512";
        final String userKey = applicationKey + hmacKey;

        try {
            // get an hmac_sha512 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(userKey.getBytes(), HMAC_SHA_512_ALGORITHM);

            // get an hmac_sha512 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA_512_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(jsonInput.getBytes());

            return Hex.encodeHexString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface MessageHandler{
        void handleMessage(String message);
    }

}