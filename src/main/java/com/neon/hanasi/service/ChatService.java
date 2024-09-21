package com.neon.hanasi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.hanasi.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

@Slf4j
@Service
public class ChatService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 메시지를 모든 세션에 비동기로 브로드캐스트합니다.
     *
     * @param chatMessage 브로드캐스트할 메시지
     * @param sessions    현재 활성화된 WebSocket 세션 집합
     */
    @Async("taskExecutor")  // 특정 스레드 풀 사용
    public void broadcastMessage(ChatMessage chatMessage, Set<WebSocketSession> sessions) {
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(chatMessage);
        } catch (Exception e) {
            log.error("메시지 JSON 변환 오류: {}", e.getMessage());
            return;
        }

        TextMessage textMessage = new TextMessage(messageJson);
        // 병렬 스트림을 사용하여 메시지 전송

        sessions.parallelStream().forEach(sess -> {
            if (sess.isOpen()) {
                int retryCount = 0;
                boolean sent = false;
                while (retryCount < 3 && !sent) {
                    try {
                        sess.sendMessage(textMessage);
                        log.debug("메시지 브로드캐스트: SESSION_ID={}, CONTENT={}", sess.getId(), chatMessage.getContent());
                        sent = true;
                    } catch (Exception e) {
                        log.error("메시지 전송 오류: SESSION_ID={}, ERROR={}, RETRY={}", sess.getId(), e.getMessage(), retryCount + 1);
                        retryCount++;
                        try {
                            Thread.sleep(100);  // 짧은 대기 후 재시도
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                if (!sent) {
                    log.error("메시지 전송 실패: SESSION_ID={}", sess.getId());
                }
            } else {
                log.warn("세션이 열려 있지 않음: SESSION_ID={}", sess.getId());
            }
        });
    }
}