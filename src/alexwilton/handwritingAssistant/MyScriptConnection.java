package alexwilton.handwritingAssistant;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class MyScriptConnection {
    private final String applicationKey, hmacKey;
    private String recognitionCloudURL;
    private StrokeAnalyser strokeAnalyser;
    private final ObjectMapper mapper = new ObjectMapper();

    public MyScriptConnection(String applicationKey, String hmacKey, String recognitionCloudURL, StrokeAnalyser strokeAnalyser) {
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.recognitionCloudURL = recognitionCloudURL;
        this.strokeAnalyser = strokeAnalyser;
        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }



    public void connecct(){

    }


}
