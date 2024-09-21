package com.neon.hanasi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import com.neon.hanasi.service.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 핸들러
 * 클라이언트와의 WebSocket 연결을 관리합니다.
 */
@Slf4j
@Component
public class ChatHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();  // Thread-safe Set
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MessagePublisher messagePublisher;

    /**
     * Redis Pub/Sub을 통해 수신된 메시지를 모든 세션에 브로드캐스트합니다.
     *
     * @param chatMessage 브로드캐스트할 메시지
     */
    public void broadcast(ChatMessage chatMessage) {
        TextMessage textMessage;
        try {
            String messageJson = objectMapper.writeValueAsString(chatMessage);
            textMessage = new TextMessage(messageJson);
        } catch (Exception e) {
            log.error("메시지 JSON 변환 오류: {}", e.getMessage());
            return;
        }

        sessions.forEach(sess -> {
            if (sess.isOpen()) {
                try {
                    sess.sendMessage(textMessage);
                    log.debug("메시지 브로드캐스트: SESSION_ID={}, CONTENT={}", sess.getId(), chatMessage.getContent());
                } catch (Exception e) {
                    log.error("메시지 전송 오류: SESSION_ID={}, ERROR={}", sess.getId(), e.getMessage());
                }
            } else {
                log.warn("세션이 열려 있지 않음: SESSION_ID={}", sess.getId());
            }
        });
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("WebSocket 연결 추가: SESSION_ID={}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 수신한 메시지를 ChatMessage 객체로 변환
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
        log.info("메시지 수신: MESSAGE_ID={}, CONTENT={}", chatMessage.getMessageId(), chatMessage.getContent());

        // 메시지를 Redis Pub/Sub을 통해 발행
        messagePublisher.publish(chatMessage);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket 연결 종료: SESSION_ID={}", session.getId());
    }
}