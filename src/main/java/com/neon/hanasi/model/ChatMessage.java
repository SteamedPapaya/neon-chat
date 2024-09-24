package com.neon.hanasi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessage {

    private String sender;
    private String content;
    private String messageId;  // 메시지의 고유 ID
    private long sentTime;  // 클라이언트에서 보내는 시간

    public ChatMessage(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.messageId = UUID.randomUUID().toString();
        this.sentTime = System.currentTimeMillis();
    }
}