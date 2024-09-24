package com.neon.hanasi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.manager.SessionManager;
import com.neon.hanasi.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSubscriber implements MessageListener {

    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    // 테스트를 위한 리스너
    private Consumer<ChatMessage> testMessageListener;

    public void setTestMessageListener(Consumer<ChatMessage> listener) {
        this.testMessageListener = listener;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            ChatMessage chatMessage = objectMapper.readValue(messageBody, ChatMessage.class);
            log.info("Redis에서 메시지 수신: {}", chatMessage.getContent());

            // 테스트 리스너 호출
            if (testMessageListener != null) {
                testMessageListener.accept(chatMessage);
            }

            String messageJson = objectMapper.writeValueAsString(chatMessage);
            sessionManager.broadcast(messageJson);
        } catch (Exception e) {
            log.error("메시지 수신 오류: {}", e.getMessage());
        }
    }
}