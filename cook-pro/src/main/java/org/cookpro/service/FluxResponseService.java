package org.cookpro.service;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import jakarta.annotation.Resource;
import org.cookpro.utils.SystemPrinter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/*
    FluxResponseService.java
    这个类目前是一个占位符，未来可以用于处理 Flux 响应相关的逻辑，例如：
        - 统一处理 Flux 响应的格式化
        - 处理 Flux 响应中的错误
        - 提供一些工具方法来简化 Flux 响应的使用
 */
@Service
public class FluxResponseService {


    @Resource
    MemoryCacheService memoryCacheService;




    public void onFluxError(String threadId, Throwable error){
        if(memoryCacheService.hasInterruptSink(threadId)){
            Sinks.Many<String> sink = memoryCacheService.getInterruptSink(threadId);
            sink.tryEmitError(error);
            memoryCacheService.removeInterruptSink(threadId);
        }
    }

}
