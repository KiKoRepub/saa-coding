package org.cookpro.utils;

import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.action.InterruptionMetadata;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import reactor.core.publisher.Flux;

public class ResponseUtils {


    public static Flux<String> handleStreamingOutput(Flux<NodeOutput> outputFlux) {
        return outputFlux
                .handle((out, sink) -> {
                    if (out instanceof InterruptionMetadata interruptionMetadata) {
                        for (InterruptionMetadata.ToolFeedback toolFeedback : interruptionMetadata.toolFeedbacks()) {
                            String toolName = toolFeedback.getName();

                            SystemPrinter.println("工具调用中断: " + toolName);
                        }
                        sink.next("[系统] 发生了工具调用中断，请等待人工审核结果...");
                        try{
                            Thread.sleep(5000);
                            InterruptionMetadata interruptionMetadata1 = HITLHelper.approveAll(interruptionMetadata);



                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    if (out instanceof StreamingOutput<?> streamingOutput) {

                        String chunk = streamingOutput.chunk();

                        if (chunk != null && !chunk.isEmpty()) {

                            SystemPrinter.println("流式chunk: " + chunk);

                            sink.next(chunk);
                        }
                    }
                });
    }

}
