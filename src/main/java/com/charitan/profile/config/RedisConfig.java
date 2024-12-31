package com.charitan.profile.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Configuration
public class RedisConfig {

//    @Value("${spring.data.redis.sentinel.master}")
//    private String sentinelMasterName;
//
//    @Value("${spring.data.redis.sentinel.nodes}")
//    private List<String> sentinelNodes;
//
//    @Value("${spring.data.redis.pool.max-active}")
//    private int maxActive;
//
//    @Value("${spring.data.redis.pool.max-idle}")
//    private int maxIdle;
//
//    @Value("${spring.data.redis.pool.min-idle}")
//    private int minIdle;
//
//    @Value("${spring.data.redis.pool.max-wait-time}")
//    private long maxWaitTime;
//
//    @Bean(name="jedisConnectionFactory")
//    public JedisConnectionFactory jedisConnectionFactory() {
//        return new JedisConnectionFactory(
//                redisSentinelConfiguration(),
//                poolConfig());
//    }
//
//    @Bean(name="poolConfig")
//    public JedisPoolConfig poolConfig() {
//        final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//        jedisPoolConfig.setTestOnBorrow(true);
//        jedisPoolConfig.setMaxTotal(maxActive);
//        jedisPoolConfig.setMaxIdle(maxIdle);
//        jedisPoolConfig.setMinIdle(minIdle);
//        jedisPoolConfig.setTestOnReturn(true);
//        jedisPoolConfig.setTestWhileIdle(true);
//        jedisPoolConfig.setMaxWaitMillis(maxWaitTime);
//        return jedisPoolConfig;
//    }
//
//    @Bean
//    public RedisSentinelConfiguration redisSentinelConfiguration() {
//        return new RedisSentinelConfiguration(sentinelMasterName,
//                new HashSet<String>(sentinelNodes));
//    }

    private final RedisProperties redisProperties;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().readFrom(ReadFrom.REPLICA_PREFERRED).build();
        RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master(redisProperties.getSentinel().getMaster());
        redisProperties.getSentinel().getNodes().forEach(s -> sentinelConfig.sentinel(redisProperties.getUrl(), Integer.valueOf(s)));
        sentinelConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
        return new LettuceConnectionFactory(sentinelConfig, clientConfig);
    }

    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().disableCachingNullValues().entryTtl(Duration.ofMinutes(30))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        redisCacheConfiguration.usePrefix();
        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory()).cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean(name = "REDIS_DONORS")
    public RedisTemplate<String, Object> donorRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean(name = "REDIS_CHARITIES")
    public RedisTemplate<String, Object> charityRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        redisTemplate.setKeySerializer(jackson2JsonRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashKeySerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public Jackson2JsonRedisSerializer jackson2JsonRedisSerializer() {
        return new Jackson2JsonRedisSerializer(String.class);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}