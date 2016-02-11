package alexwilton.handwritingAssistant;

public class MyScriptViaWebSockets {
    private final String applicationKey, hmacKey;
    private String recognitionCloudURL;
    private StrokeAnalyser strokeAnalyser;

    public MyScriptViaWebSockets(String applicationKey, String hmacKey, String recognitionCloudURL, StrokeAnalyser strokeAnalyser) {
        this.applicationKey = applicationKey;
        this.hmacKey = hmacKey;
        this.recognitionCloudURL = recognitionCloudURL;
        this.strokeAnalyser = strokeAnalyser;
    }
}
