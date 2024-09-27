package com.neon.hanasi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import com.neon.hanasi.service.MessagePublisher;
import com.neon.hanasi.manager.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;
    private final MessagePublisher messagePublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionManager.addSession(session);
        log.info("WebSocket 연결 추가: SESSION_ID={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            log.info("메시지 수신: MESSAGE_ID={}, CONTENT={}", chatMessage.getMessageId());

            // 메시지를 Redis Pub/Sub을 통해 발행
            messagePublisher.publish(chatMessage);
        } catch (Exception e) {
            log.error("메시지 처리 오류: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessionManager.removeSession(session);
        log.info("WebSocket 연결 종료: SESSION_ID={}", session.getId());
    }
}