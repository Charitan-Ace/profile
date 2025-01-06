package com.charitan.profile.config;

import com.charitan.profile.donor.internal.dtos.DonorDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.*;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.sentinel.master}")
    private String sentinelMasterName;

    @Value("${spring.data.redis.sentinel.nodes}")
    private List<String> sentinelNodes;

    @Value("${spring.data.redis.pool.max-active}")
    private int maxActive;

    @Value("${spring.data.redis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.data.redis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.data.redis.pool.max-wait-time}")
    private long maxWaitTime;

    @Bean(name="jedisConnectionFactory")
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory(
                redisSentinelConfiguration(),
                poolConfig());
    }

    @Bean(name="poolConfig")
    public JedisPoolConfig poolConfig() {
        final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMinIdle(minIdle);
        jedisPoolConfig.setTestOnReturn(true);
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setMaxWaitMillis(maxWaitTime);
        return jedisPoolConfig;
    }

    @Bean
    public RedisSentinelConfiguration redisSentinelConfiguration() {
        return new RedisSentinelConfiguration(sentinelMasterName,
                new HashSet<String>(sentinelNodes));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .build();
    }

    @Bean(name = "REDIS_DONORS")
    public RedisTemplate<String, DonorDTO> redisDonorTemplate(ObjectMapper objectMapper) {
        RedisTemplate<String, DonorDTO> template = new RedisTemplate<String, DonorDTO>();

        Jackson2JsonRedisSerializer<DonorDTO> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, DonorDTO.class);

        template.setConnectionFactory(jedisConnectionFactory());

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(jsonSerializer);
        template.setValueSerializer(jsonSerializer);

        return template;
    }

    @Bean(name = "REDIS_DONORS_ZSET")
    public RedisTemplate<String, String> redisDonorZSetTemplate(ObjectMapper objectMapper) {
        RedisTemplate<String, String> template = new RedisTemplate<String, String>();

        Jackson2JsonRedisSerializer<String> jsonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, String.class);

        template.setConnectionFactory(jedisConnectionFactory());

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setHashValueSerializer(jsonSerializer);
        template.setValueSerializer(jsonSerializer);

        return template;
    }

    @Bean(name = "REDIS_CHARITIES")
    public RedisTemplate<String, Object> charityRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(jedisConnectionFactory());
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(genericJackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());  // Ensure hash keys are serialized as strings
        redisTemplate.setHashValueSerializer(genericJackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}