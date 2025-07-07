package ru.danilgordienko.film_storage.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
//import org.springframework.data.redis.serializer.ByteArrayRedisSerializer;



@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    @Primary
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(3))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    // Кэш-менеджер для бинарных данных (постеры)
    @Bean
    public RedisCacheManager binaryCacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))  // Долгий TTL для постеров
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new CustomByteArraySerializer()))  // Сериализатор для byte[]
                .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

}

class CustomByteArraySerializer implements RedisSerializer<byte[]> {
    @Override
    public byte[] serialize(byte[] bytes) {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) {
        return bytes;
    }
}