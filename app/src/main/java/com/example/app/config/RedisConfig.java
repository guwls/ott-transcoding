package com.example.app.config;

import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.core.io.ClassPathResource;
import java.util.List;

@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() { return new LettuceConnectionFactory(); }

    @Bean
    public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory cf) {
        var tpl = new StringRedisTemplate(cf);
        tpl.setKeySerializer(new StringRedisSerializer());
        tpl.setValueSerializer(new StringRedisSerializer());
        return tpl;
    }

    @Bean
    public DefaultRedisScript<List> tokenBucketScript() {
        var script = new DefaultRedisScript<List>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/token_bucket.lua")));
        script.setResultType(List.class);
        return script;
    }
}