package alexwilton.handwritingAssistant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.myscript.cloud.sample.ws.api.Stroke;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.drafts.Draft_75;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.net.URI;
import java.net.URISyntaxException;

public class MyScriptConnection {
    private final String applicationKey, hmacKey, recognitionCloudURL;
    private StrokeAnalyser strokeAnalyser;
    private final ObjectMapper mapper = new ObjectMapper();
    private WebSocketClient webSocketClient;

    public MyScriptConnection() {
        applicationKey = "c34e7a84-a0da-41cb-84f8-b2cf8459c3df";
        hmacKey = "667dc91d-ce7a-4074-a74e-a4ea0a8455b8";
        recognitionCloudURL = "wss://cloud.myscript.com/api/v3.0/recognition/ws/text";
        setup();
    }

    public MyScriptConnection(String applicationKey, String hmacKey, String recognitionCloudURL, StrokeAnalyser strokeAnalyser) {
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.recognitionCloudURL = recognitionCloudURL;
        this.strokeAnalyser = strokeAnalyser;
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void setup(){
        try {
            webSocketClient = new WebSocketClient(new URI(recognitionCloudURL), new Draft_17()) {
                @Override
                public void onMessage(String message) {
                    JSONObject obj = (JSONObject) JSONValue.parse(message);
                    String channel = (String) obj.get("channel");
                    System.out.println("Message Received: " + channel);
                }

                @Override
                public void onOpen(ServerHandshake handshake) {
                    System.out.println("opened connection");
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("closed connection");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }

            };
            webSocketClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
//    public void recognizeUsingWebSockets(Stroke[] strokes) throws Exception {
//        WebSocketClient
//        final String input = getTextInput(strokes);
//
////        HttpURLConnection connection = openConnection(new URL(recognitionCloudURL));
////        OutputStream output = connection.getOutputStream();
//        byte[] encodedStrToSend = String.format("applicationKey=%s&hmacKey=%s&textInput=%s", applicationKey, hmacKey, input).getBytes(UTF_8);
//        webSocketClient.connect();

//
//    }


    public void connect(){
        JSONObject init1 = new JSONObject();
        init1.put("type", "applicationKey");
        init1.put("applicationKey", applicationKey);
//        webSocketClient.send(init1.toJSONString());
    }

    public static void main(String[] args){
        MyScriptConnection conn = new MyScriptConnection();
        conn.connect();
    }
}
