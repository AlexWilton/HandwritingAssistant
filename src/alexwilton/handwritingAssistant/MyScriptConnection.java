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
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class MyScriptConnection {
    private MessageHandler messageHandler;
    Session userSession = null;
    private ObjectMapper mapper = new ObjectMapper();
    private String applicationKey, hmacKey, myScriptURL;

    enum ConnectionStatus { DISCONNECTED, READY, BUSY}
    private ConnectionStatus status = ConnectionStatus.DISCONNECTED;

    public MyScriptConnection(String applicationKey, String hmacKey, String myScriptURL){
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.myScriptURL = myScriptURL;
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        connect();
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
            throw new RuntimeException(e);
        }
    }
    @OnOpen
    public void onOpen(Session userSession) {this.userSession = userSession; status = ConnectionStatus.READY;}
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
        status = ConnectionStatus.DISCONNECTED;
        System.out.println("Connection to MyScript closed!");
    }
    @OnMessage
    public void onMessage(String jsonMsg) {
        System.out.println("Message received: " + jsonMsg);
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
                break;
            case "textResult":
                if(messageHandler != null)
                    messageHandler.handleMessage(jsonMsg);
                else
                    System.out.println("Result received: " + getTextOutput(jsonMsg));
                sendResetRequest();
                break;
        }
    }

    private synchronized void sendResetRequest() {
        waitForReady();
        JSONObject reset = new JSONObject();
        reset.put("type", "reset");
        System.out.println("Sent: Reset Request");
        sendMessage(reset.toJSONString());
        notify();
    }

    private synchronized void sendStartRecogRequest(Stroke[] strokes){
        waitForReady();
        JSONObject start1 = getTextInput(strokes);
        start1.put("type", "start");
        JSONObject textParam = new JSONObject();
        textParam.put("language", "en_GB");
        textParam.put("textInputMode", "CURSIVE");
        textParam.put("textProperties", new JSONObject());
        start1.put("textParameter", textParam);
        sendMessage(start1.toJSONString());
        notify();
    }

    private synchronized void waitForReady(){
        switch (status){
            case DISCONNECTED: connect();
            case BUSY:
                while(status != ConnectionStatus.READY){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    };
                }
                break;
            case READY: break;
        }
    }

    protected String getTextOutput(String json) {

        Reader jsonReader = new StringReader(json);
        TextOutput output = null;
        try {
            output = mapper.readValue(jsonReader, TextOutput.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final int selectedCandidateIdx = output.getResult().getTextSegmentResult().getSelectedCandidateIdx();
        return output.getResult().getTextSegmentResult().getCandidates().get(selectedCandidateIdx).getLabel();
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



    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }
    public void sendMessage(String message) {
        synchronized (this) {
            System.out.println("Sent: " + message);
            this.userSession.getAsyncRemote().sendText(message);
        }
    }

    public void recognizeStrokes(Stroke[] strokeArray) {
        sendStartRecogRequest(strokeArray);
    }

    /**
     * Compute required value to return to the server with hmac SHA512 hash algorithm
     * It prevents from man-in-the-middle key theft
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


    public static void main( String[] args ) throws URISyntaxException, InterruptedException {
        String applicationKey = "c34e7a84-a0da-41cb-84f8-b2cf8459c3df";
        String hmacKey = "667dc91d-ce7a-4074-a74e-a4ea0a8455b8";
        String url = "ws://cloud.myscript.com/api/v3.0/recognition/ws/text";

        JSONObject init1 = new JSONObject();
        init1.put("type", "applicationKey");
        init1.put("applicationKey", applicationKey);
        System.out.println(init1.toJSONString());

        MyScriptConnection clientEndPoint = new MyScriptConnection(applicationKey, hmacKey, url);
//        clientEndPoint.addMessageHandler(new MyScriptConnection.MessageHandler() {
//            public void handleMessage(String message) {
//                System.out.println("Message Received: " + message);
//            }
//        });
        clientEndPoint.sendMessage(init1.toJSONString());
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();

    }

}