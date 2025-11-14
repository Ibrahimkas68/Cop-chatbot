package com.assistant.smartsearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisRateLimitConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
