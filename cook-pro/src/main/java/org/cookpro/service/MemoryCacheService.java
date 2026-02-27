package org.cookpro.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Sinks;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
  内存缓存服务，用于一些特殊数据的缓存
    1. Sinks.Many<String> 用于线程中断的信号传递
 */
@Service
public class MemoryCacheService {



    private final Map<String, Sinks.Many<String>> threadInterruptSinks = new ConcurrentHashMap<>();

    public void cacheInterruptSink(String threadId, Sinks.Many<String> sink) {
        threadInterruptSinks.put(threadId, sink);
    }

    public boolean hasInterruptSink(String threadId) {
        return threadInterruptSinks.containsKey(threadId);
    }
    public Sinks.Many<String> getInterruptSink(String threadId) {
        return threadInterruptSinks.get(threadId);
    }



    public void removeInterruptSink(String threadId) {
        threadInterruptSinks.remove(threadId);
    }
}
