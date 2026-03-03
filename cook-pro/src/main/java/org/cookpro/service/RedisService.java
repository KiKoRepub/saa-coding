package org.cookpro.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /* ==============================
     * 基础 String 缓存
     * ============================== */

    public void cacheOneString(String cacheKey, String needCache, Long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(cacheKey, needCache, ttl, timeUnit);
    }

    public void cacheOneString(String cacheKey, String needCache) {
        redisTemplate.opsForValue().set(cacheKey, needCache);
    }

    public String getString(String cacheKey) {
        Object value = redisTemplate.opsForValue().get(cacheKey);
        return value == null ? null : value.toString();
    }

    public boolean exists(String cacheKey) {
        Boolean result = redisTemplate.hasKey(cacheKey);
        return Boolean.TRUE.equals(result);
    }

    public void delete(String cacheKey) {
        redisTemplate.delete(cacheKey);
    }

    public Long getExpire(String cacheKey) {
        return redisTemplate.getExpire(cacheKey);
    }

    public Boolean expire(String cacheKey, Long ttl, TimeUnit timeUnit) {
        return redisTemplate.expire(cacheKey, ttl, timeUnit);
    }

    /* ==============================
     * Object 缓存（推荐 JSON 方式）
     * ============================== */

    public void cacheObject(String cacheKey, Object value, Long ttl, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(cacheKey, json, ttl, timeUnit);
        } catch (Exception e) {
            log.error("缓存对象失败 key={}", cacheKey, e);
        }
    }

    public void cacheObject(String cacheKey, Object value) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(cacheKey, json);
        } catch (Exception e) {
            log.error("缓存对象失败 key={}", cacheKey, e);
        }
    }

    public <T> T getCacheObject(String cacheKey, Class<T> clazz) {
        try {
            Object o = redisTemplate.opsForValue().get(cacheKey);
            if (o == null) {
                return null;
            }

            if (o instanceof String oo) {
                return objectMapper.readValue(oo, clazz);
            }

            return objectMapper.convertValue(o, clazz);
        } catch (Exception e) {
            log.error("获取缓存对象失败 key={}", cacheKey, e);
            return null;
        }
    }

    /* ==============================
     * List 操作
     * ============================== */

    public void cacheList(String cacheKey, List<?> list, Long ttl, TimeUnit timeUnit) {
        try {
            String json = objectMapper.writeValueAsString(list);
            redisTemplate.opsForValue().set(cacheKey, json, ttl, timeUnit);
        } catch (Exception e) {
            log.error("缓存List失败 key={}", cacheKey, e);
        }
    }

    public <T> List<T> getCacheList(String cacheKey, Class<T> clazz) {
        try {
            Object o = redisTemplate.opsForValue().get(cacheKey);
            if (o == null) {
                return Collections.emptyList();
            }

            JavaType javaType = objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, clazz);

            return objectMapper.readValue(o.toString(), javaType);
        } catch (Exception e) {
            log.error("获取List缓存失败 key={}", cacheKey, e);
            return Collections.emptyList();
        }
    }

    /* ==============================
     * Hash 操作
     * ============================== */

    public void putHash(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    public Object getHash(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    public Map<Object, Object> getAllHash(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public void deleteHash(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key, hashKey);
    }

    /* ==============================
     * 自增操作（常用于计数器）
     * ============================== */

    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }


    public <T> T getWithCacheLoader(
            String key,
            Class<T> clazz,
            Supplier<T> dbLoader,
            Long ttl,
            TimeUnit timeUnit) {
        // 旁路缓存：先查缓存，缓存没有再查数据库，并将结果写入缓存

        T cache = getCacheObject(key, clazz);

        if (cache != null) {
            return cache;
        }

        T value = dbLoader.get();

        if (value != null) {
            cacheObject(key, value, ttl, timeUnit);
        } else {
            // 防止缓存穿透
            cacheOneString(key, "", 1, TimeUnit.MINUTES);
        }

        return value;
    }
}
