package com.neon.hanasi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import com.neon.hanasi.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();  // Thread-safe Set

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

        // 메시지 처리를 ChatService에 비동기로 위임
        chatService.broadcastMessage(chatMessage, sessions);

        // 모든 세션에 메시지 브로드캐스트
        for (WebSocketSession sess : sessions) {
            try {
                sess.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                log.debug("SESSION={} MESSAGE={}", sess.getId(), chatMessage.getContent());
            } catch (IllegalStateException e) {
                log.error("ERROR={}", e.getMessage());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
        log.info("WebSocket 연결 종료: SESSION_ID={}", session.getId());
    }
}