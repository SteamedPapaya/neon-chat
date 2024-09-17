package com.neon.hanasi.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ChatHandlerTest {

    private ChatHandler chatHandler;
    private WebSocketSession session1;
    private WebSocketSession session2;

    @BeforeEach
    void setUp() {
        chatHandler = new ChatHandler();
        session1 = mock(WebSocketSession.class);
        session2 = mock(WebSocketSession.class);
    }

    @Test
    void handleTextMessage_shouldBroadcastMessage() throws Exception {
        // given
        chatHandler.afterConnectionEstablished(session1);
        chatHandler.afterConnectionEstablished(session2);
        TextMessage message = new TextMessage("Hello World");

        // when
        chatHandler.handleTextMessage(session1, message);

        // then
        verify(session1).sendMessage(message);
        verify(session2).sendMessage(message);
    }
}