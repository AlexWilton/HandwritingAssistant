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

/**
 * Class Manages connection to MyScript Cloud's stroke recognition service.
 */
@ClientEndpoint
public class MyScriptConnection {
    /**
     * Message Handler defines what to do with recognistion results received from MyScript.
     */
    private MessageHandler messageHandler;

    /**
     * Connection Session to MyScript.
     */
    private Session userSession = null;

    /**
     * Object Mapper maps MyScript response json to Java objects.
     */
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Allowed Statuses of Connection to MyScript Cloud Recognition.
     */
    enum ConnectionStatus { DISCONNECTED, READY_TO_START, STARTED}

    /**
     * Current connection status to MyScript Cloud Recognition.
     */
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    /**
     * MyScript Cloud Recognition Application Key
     */
    private String applicationKey;

    /**
     * MyScript Cloud Recognition HMAC Key
     */
    private String hmacKey;

    /**
     * MyScript Cloud Recognition URL
     */
    private String myScriptURL;

    /**
     * Create MyScript Connection instance. Requires necessary connection information (provided by MyScript)
     * @param applicationKey Application Key
     * @param hmacKey HMAC Key
     * @param myScriptURL MyScript Cloud Recognition URL
     */
    public MyScriptConnection(String applicationKey, String hmacKey, String myScriptURL){
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.myScriptURL = myScriptURL;
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        connect();
    }

    /**
     * Attempt to connect to MyScript Cloud Recognition.
     */
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

    /**
     * On successful connection to MyScript Cloud Recognition, keep track of successful session.
     * @param userSession
     */
    @OnOpen
    public void onOpen(Session userSession) {this.userSession = userSession;}

    /**
     * On closing of current connection to MyScript Cloud Recognition, discard current session.
     * @param userSession Old Session
     * @param reason Reason for session closing.
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        status = ConnectionStatus.DISCONNECTED;
    }

    /**
     * On receiving message from MyScript Cloud Recognition, decide how to respond based on received message type.
     * @param jsonMsg Message received from MyScript Cloud Recognition as json.
     */
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
                if(messageHandler != null) {
                    String textRecognised = getTextOutputResult(jsonMsg);
                    messageHandler.handleMessage(textRecognised);
                }else
                    System.out.println("Result received: " + getTextOutputResult(jsonMsg));
                sendResetRequest();
                break;
        }
    }

    /**
     * Send request to reset current recognition session.
     */
    private void sendResetRequest() {
        JSONObject reset = new JSONObject();
        reset.put("type", "reset");
        sendMessage(reset.toJSONString());
    }

    /**
     * Send start recognition request for provided strokes.
     * Waits for connection to be ready before sending request.
     * @param strokes Stroke to request analysis of.
     */
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

    /**
     * Wait for connection to be ready to start recognising new strokes.
     */
    private void waitForReady(){
        switch (status){
            case DISCONNECTED: connect();
            case STARTED:
                while(status != ConnectionStatus.READY_TO_START){
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case READY_TO_START: break;
        }
    }

    /**
     * From a json response from MyScript, extract text output.
     * @param json json response from MyScript.
     * @return
     */
    protected String getTextOutputResult(String json) {
        TextOutput output = getTextOutputs(json);
        final int selectedCandidateIdx = output.getResult().getTextSegmentResult().getSelectedCandidateIdx();
        return output.getResult().getTextSegmentResult().getCandidates().get(selectedCandidateIdx).getLabel();
    }

    /**
     * Use mapper to construct java objects from MyScript's json response.
     * @param json json response from MyScript.
     * @return TextOutput object constructed from MyScript response json.
     */
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

    /**
     * Constructs a text input object from strokes for sending to MyScript.
     * @param strokes Strokes to be analysed.
     * @return JSONObject in MyScript's desired format.
     */
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

    /**
     * Send Asynchronous message to MyScript.
     * @param message Message as String to send to MyScript.
     */
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
        waitForReady();
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

    /**
     * Blocking method which waits for current recognition to be finished.
     */
    public void waitUntilFinished() {
        try {
            while(status == ConnectionStatus.STARTED)
                Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}