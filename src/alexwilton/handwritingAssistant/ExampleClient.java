package alexwilton.handwritingAssistant;

import org.json.simple.JSONObject;

import javax.websocket.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

/**
 * ChatServer Client
 *
 * @author Jiji_Sasidharan
 */
@ClientEndpoint
public class ExampleClient {
    Session userSession = null;
    private MessageHandler messageHandler;

    public ExampleClient(URI endpointURI) {
        try {
            WebSocketContainer container = ContainerProvider
                    .getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession
     *            the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     *
     * @param message
     *            The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null)
            this.messageHandler.handleMessage(message);
    }

    /**
     * register message handler
     *
     * @param message
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

    /**
     * Message handler.
     *
     * @author Jiji_Sasidharan
     */
    public static interface MessageHandler {
        public void handleMessage(String message);
    }

    public static void main( String[] args ) throws URISyntaxException, InterruptedException {
        String applicationKey = "c34e7a84-a0da-41cb-84f8-b2cf8459c3df";
        String hmacKey = "667dc91d-ce7a-4074-a74e-a4ea0a8455b8";
        String url = "ws://cloud.myscript.com/api/v3.0/recognition/ws/text";

//        Map<String, String> headers = new HashMap<>();
//        headers.put("type", "applicationKey");
//        headers.put("applicationKey", applicationKey);
//        ExampleClient c = new ExampleClient( new URI( url ));
//
        JSONObject init1 = new JSONObject();
        init1.put("type", "applicationKey");
        init1.put("applicationKey", applicationKey);
        System.out.println(init1.toJSONString());

        ExampleClient clientEndPoint = new ExampleClient(new URI(url));
        clientEndPoint.addMessageHandler(new ExampleClient.MessageHandler() {
            public void handleMessage(String message) {
                System.out.println("Message Received: " + message);
            }
        });
        clientEndPoint.sendMessage(init1.toJSONString());

        while(true){}
    }

}