package com.neon.hanasi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.GenericContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2.1")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setRedisProperties(DynamicPropertyRegistry registry) {
        redisContainer.start();  // 컨테이너 시작
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    @Test
    public void testWebSocketMessaging() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String testMessageContent = "Hello, WebSocket!";
        ChatMessage testMessage = new ChatMessage("tester", testMessageContent);

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        client.doHandshake(new TextWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                String messageJson = objectMapper.writeValueAsString(testMessage);
                session.sendMessage(new TextMessage(messageJson));
            }

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                ChatMessage receivedMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
                assertEquals(testMessageContent, receivedMessage.getContent());
                latch.countDown();
            }
        }, headers, new URI("ws://localhost:" + port + "/chat")).get();

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "WebSocket을 통해 메시지를 받지 못했습니다.");
    }
}