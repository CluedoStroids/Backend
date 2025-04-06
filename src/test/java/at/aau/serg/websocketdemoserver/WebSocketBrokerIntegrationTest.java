package at.aau.serg.websocketdemoserver;

import at.aau.serg.websocketdemoserver.websocket.StompFrameHandlerClientImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketBrokerIntegrationTest {

    @LocalServerPort
    private int port;

    private final String WEBSOCKET_URI = "ws://localhost:%d/cluedo";
    private final String WEBSOCKET_TOPIC = "/topic/";

    private StompSession session;

    @BeforeEach
    void setUp() {
        try {
            session = initStompSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testWebSocketLogin() throws InterruptedException {
        BlockingQueue<String> logins = new LinkedBlockingDeque<>();
        session.subscribe(WEBSOCKET_TOPIC+"login-response", new StompFrameHandlerClientImpl(logins));
        String username = "User";
        session.send("/app/login",username);
        assertEquals("login-response",logins.poll(3, TimeUnit.SECONDS));
    }

    @Test
    public void testWebSocketDisconnect() throws InterruptedException {
        BlockingQueue<String> disconnects = new LinkedBlockingDeque<>();
        session.subscribe(WEBSOCKET_TOPIC+"disconnect-response", new StompFrameHandlerClientImpl(disconnects));
        String username = "User";
        session.send("/app/disconnect",username);
        assertEquals("disconnect-response",disconnects.poll(3, TimeUnit.SECONDS));
    }


    @Test
    public void testWebSocketMessageBroker() throws InterruptedException {
        BlockingQueue<String> messages = new LinkedBlockingDeque<>();
        session.subscribe(WEBSOCKET_TOPIC+"message-response", new StompFrameHandlerClientImpl(messages));
        // send a message to the server
        String message = "Test message";
        session.send("/app/message", message);

        var expectedResponse = "echo from broker: " + message;
        assertThat(messages.poll(1, TimeUnit.SECONDS)).isEqualTo(expectedResponse);
    }

    /**
     * @return The Stomp session for the WebSocket connection (Stomp - WebSocket is comparable to HTTP - TCP).
     */
    public StompSession initStompSession() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new StringMessageConverter());

        // connect client to the websocket server
        StompSession session = stompClient.connectAsync(String.format(WEBSOCKET_URI, port),
                        new StompSessionHandlerAdapter() {
                        })
                // wait 1 sec for the client to be connected
                .get(1, TimeUnit.SECONDS);

        return session;
    }

}
