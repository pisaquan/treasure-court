package com.sancai.oa.core.redis;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Map;

/**
 * @Author wangyl
 * @create 2019/7/24 09:39
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer serializer = new Jackson2JsonRedisSerializer(Object.class);
        template.setDefaultSerializer(serializer);
        return template;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory) throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        FastJsonRedisSerializer serializer = new FastJsonRedisSerializer(Object.class);
        template.setValueSerializer(serializer);
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }


    /**
     * @Primary  配置默认的缓存管理器
     *    valueSerializationPair：使用默认的JdkSerializationRedisSerializer()
     *    使用方式：如果不指定cacheManagers属性，就会使用默认的CacheManager
     *    @Cacheable(value = "cache_1_minutes",keyGenerator = "myKeyGenerator")
     * @param redisConnectionFactory
     * @return
     */
    @Primary
    @Bean
    public RedisCacheManager defaultCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(redisCacheConfiguration(null, Duration.ofDays(30)));


//        //可以抽取的公共配置
//        Map<String, RedisCacheConfiguration> map = ImmutableMap.<String,RedisCacheConfiguration>builder()
//                .put("cache_1_minutes", redisCacheConfiguration(null, Duration.ofMinutes(1)))
//                .put("cache_10_minutes", redisCacheConfiguration(null, Duration.ofMinutes(10)))
//                .put("cache_1_hour", redisCacheConfiguration(null, Duration.ofHours(1)))
//                .put("cache_10_hour", redisCacheConfiguration(null,Duration.ofHours(10)))
//                .put("cache_1_day", redisCacheConfiguration(null,Duration.ofDays(1)))
//                .put("cache_7_days", redisCacheConfiguration(null,Duration.ofDays(7)))
//                .put("cache_30_days", redisCacheConfiguration(null,Duration.ofDays(30)))
//                .build();

//        builder.withInitialCacheConfigurations(map);

        return builder.build();
    }

    /**
     * 启用@Cacheable等注解时，redis里面用到的key--value的序列化
     *                  key = new StringRedisSerializer()
     *                  value = new JdkSerializationRedisSerializer()
     *  以及缓存的时效
     *
     *
     * @return
     */
    public RedisCacheConfiguration redisCacheConfiguration(RedisSerializer serializer, Duration duration){

        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig();
        if (null != serializer) {
            configuration = configuration.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
        }
        //key的前缀不添加 cacheNames
        configuration = configuration.disableKeyPrefix();
        //设置缓存的时效
        configuration = configuration.entryTtl(duration);
        //configuration.s
        return configuration;
    }

}
