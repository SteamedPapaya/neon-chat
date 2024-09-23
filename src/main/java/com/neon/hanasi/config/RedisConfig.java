package com.neon.hanasi.config;

import com.neon.hanasi.service.MessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * RedisTemplate 설정
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return RedisTemplate 인스턴스
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 직렬화 방식 설정: StringRedisSerializer로 키, JSON Serializer로 값 처리
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    /**
     * Redis 메시지 리스너 컨테이너 설정
     *
     * @param connectionFactory Redis 연결 팩토리
     * @param messageSubscriber 메시지 리스너 (MessageSubscriber)
     * @param chatChannel       채팅 채널 (ChannelTopic)
     * @return RedisMessageListenerContainer 인스턴스
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       MessageSubscriber messageSubscriber,
                                                                       ChannelTopic chatChannel) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 메시지 리스너 및 채널 등록
        container.addMessageListener(messageSubscriber, chatChannel);

        return container;
    }

    /**
     * 채팅 메시지를 위한 Redis 채널 설정
     *
     * @return ChannelTopic 인스턴스
     */
    @Bean
    public ChannelTopic chatChannel() {
        return new ChannelTopic("chat_channel");
    }
}