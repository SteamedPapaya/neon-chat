package com.neon.hanasi;

import com.neon.hanasi.model.ChatMessage;
import com.neon.hanasi.service.MessagePublisher;
import com.neon.hanasi.service.MessageSubscriber;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.GenericContainer;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RedisPubSubIntegrationTest {

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7.2.1")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setRedisProperties(DynamicPropertyRegistry registry) {
        redisContainer.start();  // 컨테이너 시작
        registry.add("spring.data.redis.host", () -> redisContainer.getHost());
        registry.add("spring.data.redis.port", () -> redisContainer.getFirstMappedPort());
    }

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    private MessageSubscriber messageSubscriber;

    @Test
    public void testRedisPubSub() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String testMessageContent = "Hello, Redis!";
        ChatMessage testMessage = new ChatMessage("tester", testMessageContent);

        // 테스트 리스너 설정
        messageSubscriber.setTestMessageListener(chatMessage -> {
            assertEquals(testMessageContent, chatMessage.getContent());
            latch.countDown();
        });

        // 메시지 발행
        messagePublisher.publish(testMessage);

        boolean messageReceived = latch.await(5, TimeUnit.SECONDS);
        assertTrue(messageReceived, "Redis Pub/Sub을 통해 메시지를 받지 못했습니다.");
    }
}