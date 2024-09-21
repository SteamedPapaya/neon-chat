package com.neon.hanasi.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import com.neon.hanasi.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChatHandlerTest {

    private ChatHandler chatHandler;
    private WebSocketSession session1;
    private WebSocketSession session2;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ChatService chatService = new ChatService();
        chatHandler = new ChatHandler(chatService);
        session1 = mock(WebSocketSession.class);
        session2 = mock(WebSocketSession.class);
        objectMapper = new ObjectMapper();
    }

    @Test
    void handleTextMessage_shouldBroadcastMessage() throws Exception {
        // given
        chatHandler.afterConnectionEstablished(session1);
        chatHandler.afterConnectionEstablished(session2);

        ChatMessage chatMessage = new ChatMessage("User1", "Hello, World!", UUID.randomUUID().toString(), System.currentTimeMillis());
        String payload = objectMapper.writeValueAsString(chatMessage);
        TextMessage message = new TextMessage(payload);

        // when
        chatHandler.handleTextMessage(session1, message);

        // then
        ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);

        // 세션마다 메시지 전송 여부 확인
        verify(session1, times(1)).sendMessage(messageCaptor.capture());
        verify(session2, times(1)).sendMessage(messageCaptor.capture());

        // 전송된 메시지 내용 검증
        for (TextMessage sentMessage : messageCaptor.getAllValues()) {
            ChatMessage sentChatMessage = objectMapper.readValue(sentMessage.getPayload(), ChatMessage.class);
            assertEquals("User1", sentChatMessage.getSender());
            assertEquals("Hello, World!", sentChatMessage.getContent());
        }
    }
}