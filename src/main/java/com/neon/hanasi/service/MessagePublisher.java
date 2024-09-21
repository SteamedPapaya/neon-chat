package com.neon.hanasi.service;

import com.neon.hanasi.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

/**
 * 메시지 발행 서비스
 * Redis Pub/Sub을 통해 메시지를 발행합니다.
 */
@Service
public class MessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic chatChannel;

    @Autowired
    public MessagePublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic chatChannel) {
        this.redisTemplate = redisTemplate;
        this.chatChannel = chatChannel;
    }

    /**
     * 채팅 메시지를 Redis Pub/Sub 채널에 발행합니다.
     *
     * @param message 발행할 채팅 메시지
     */
    public void publish(ChatMessage message) {
        redisTemplate.convertAndSend(chatChannel.getTopic(), message);
    }
}