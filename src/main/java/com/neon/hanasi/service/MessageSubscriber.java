package com.neon.hanasi.service;

import com.neon.hanasi.handler.ChatHandler;
import com.neon.hanasi.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

/**
 * 메시지 구독 리스너
 * Redis Pub/Sub을 통해 수신된 메시지를 처리합니다.
 */
@Slf4j
@Service
public class MessageSubscriber implements MessageListener {

    private final ChatHandler chatHandler;
    private final ObjectMapper objectMapper;

    @Autowired
    public MessageSubscriber(ChatHandler chatHandler) {
        this.chatHandler = chatHandler;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody(), "UTF-8");
            ChatMessage chatMessage = objectMapper.readValue(messageBody, ChatMessage.class);
            log.info("Redis에서 메시지 수신: {}", chatMessage.getContent());

            // 모든 세션에 메시지 브로드캐스트
            chatHandler.broadcast(chatMessage);
        } catch (Exception e) {
            log.error("메시지 수신 오류: {}", e.getMessage());
        }
    }
}