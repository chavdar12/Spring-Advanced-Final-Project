package bg.softuni.tradezone.config;

import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  RedisCacheManagerBuilderCustomizer redisCaches() {
    RedisCacheConfiguration json =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(SerializationPair.fromSerializer(RedisSerializer.json()));
    // aggregate counts change with every new ad, so those caches stay short-lived
    return builder ->
        builder
            .cacheDefaults(json.entryTtl(Duration.ofHours(1)))
            .withCacheConfiguration("regions", json.entryTtl(Duration.ofMinutes(1)))
            .withCacheConfiguration("townsInRegion", json.entryTtl(Duration.ofMinutes(1)));
  }
}
