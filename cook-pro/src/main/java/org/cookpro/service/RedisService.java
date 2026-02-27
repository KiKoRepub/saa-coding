package org.cookpro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.SynchronousSink;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String HITL_INTERRUPT_PREFIX = "hitl:interrupt:";
    private static final Long HITL_INTERRUPT_TTL = 60 * 30L; // 30分钟


}
