//package com.myscript.cloud.sample.ws;
//
//import alexwilton.handwritingAssistant.MyScriptConnection;
//import alexwilton.handwritingAssistant.StrokeAnalyser;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.util.JSONPObject;
//import com.myscript.cloud.sample.ws.api.Point;
//import com.myscript.cloud.sample.ws.api.Stroke;
//import com.myscript.cloud.sample.ws.api.text.TextInput;
//import com.myscript.cloud.sample.ws.api.text.TextOutput;
//import org.java_websocket.WebSocket;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.drafts.Draft;
//import org.java_websocket.drafts.Draft_17;
//import org.java_websocket.exceptions.InvalidDataException;
//import org.java_websocket.handshake.ClientHandshake;
//import org.java_websocket.handshake.ServerHandshake;
//import org.java_websocket.handshake.ServerHandshakeBuilder;
//import org.json.simple.JSONObject;
//
//import java.io.*;
//import java.net.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MyScriptCloud {
//    protected static final String UTF_8 = "UTF-8";
//    private final String applicationKey, hmacKey;
//    private final ObjectMapper mapper = new ObjectMapper();
//    private String recognitionCloudURL;
//
//    public MyScriptCloud(final String recognitionCloudURL, final String applicationKey, final String hmacKey) {
//        this.recognitionCloudURL = recognitionCloudURL;
//        this.applicationKey = applicationKey;
//        this.hmacKey = hmacKey;
//        this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//    }
//
//    public static String readResponse(InputStream responseStream)
//            throws IOException {
//        StringBuffer response = new StringBuffer();
//        byte[] data = new byte[2048];
//        while (true) {
//            int length = responseStream.read(data);
//            if (length > 0) {
//                response.append(new String(data, 0, length, UTF_8));
//            } else {
//                break;
//            }
//        }
//
//        return response.toString();
//    }
//
//    protected String getPostData(String textInput) {
//        try {
//            return URLEncoder.encode(textInput.toString(), UTF_8);
//        } catch (UnsupportedEncodingException e) {
//            return null;
//        }
//    }
//
//    protected String getTextOutputResult(String json) {
//
//        Reader jsonReader = new StringReader(json);
//        TextOutput output = null;
//        try {
//            output = mapper.readValue(jsonReader, TextOutput.class);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        final int selectedCandidateIdx = output.getResult().getTextSegmentResult().getSelectedCandidateIdx();
//        return output.getResult().getTextSegmentResult().getCandidates().get(selectedCandidateIdx).getLabel();
//    }
//
//    protected HttpURLConnection openConnection(URL url) throws IOException {
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setDoOutput(true);
//        connection.setDoInput(true);
//        connection.setRequestProperty("Content-Type",
//                "application/x-www-form-urlencoded; charset=" + UTF_8);
//        connection.setRequestProperty(
//                "User-Agent",
//                "Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/28.0.1500.71 Chrome/28.0.1500.71 Safari/537.36");
//        connection.setRequestMethod("POST");
//        return connection;
//    }
//
//    public String recognize(Stroke[] strokes) throws IOException {
//        final String input = getTextInput(strokes);
//
//        HttpURLConnection connection = openConnection(new URL(recognitionCloudURL));
//        OutputStream output = connection.getOutputStream();
//
////        System.out.println("Sent: " + String.format("applicationKey=%s&hmacKey=%s&textInput=%s", applicationKey, hmacKey, input));
//
//        output.write(String.format("applicationKey=%s&hmacKey=%s&textInput=%s", applicationKey, hmacKey, input).getBytes(UTF_8));
//
//        String postData = getPostData(input);
//        output.write(postData.getBytes(UTF_8));
//        output.flush();
//        output.close();
//
//        int responseCode = connection.getResponseCode();
//        InputStream responseStream = (responseCode == HttpURLConnection.HTTP_BAD_REQUEST) ? connection.getErrorStream() : connection.getInputStream();
//
//        String json = URLDecoder.decode(readResponse(responseStream), UTF_8);
//
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            System.out.println("Response OK : " + json);
//            return getTextOutputResult(json);
//        } else {
//            System.err.println("HTTP Error: " + responseCode + " " + connection.getResponseMessage());
//            return null;
//        }
//    }
//
//    private String getTextInput(Stroke[] strokes) {
//        TextInput input = new TextInput();
//
//        for (int strokeIndex = 0; strokeIndex < strokes.length; strokeIndex++) {
//            input.addComponent();
//            for (final Point point : strokes[strokeIndex].getPoints())
//                input.addComponentPoint(point.x, point.y);
//        }
//
//        Writer jsonWriter = new StringWriter();
//        try {
//            mapper.writeValue(jsonWriter, input);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return jsonWriter.toString();
//    }
//
//
//}
