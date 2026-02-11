package org.cookpro.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE服务器，用于管理用户的SSE连接
 */
@Slf4j
public class SSEServer {


    private static final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();


    /**
     * 创建SSE连接
     * @param userId 用户ID
     * @return SseEmitter对象
     */
    public static SseEmitter connect(Long userId) {
        try {
            // timeout 设置为0，表示永不超时
            SseEmitter emitter = new SseEmitter(0L);

            emitter.onTimeout(onTimeoutCallback(userId));
            emitter.onError(onErrorCallback(userId));
            emitter.onCompletion(onCompletionCallback(userId));

            userEmitters.put(userId, emitter);
            log.info("SSE连接成功: userId={}", userId);

            return emitter;
        } catch (Exception e) {
            log.error("SSE连接失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取指定用户的SseEmitter
     * @param userId 用户ID
     * @return SseEmitter对象，如果不存在则返回null
     */
    public static SseEmitter getEmitter(String userId) {
        return userEmitters.get(userId);
    }

    /**
     * 发送消息给指定用户
     * @param userId 用户ID
     * @param eventName 事件名称
     * @param data 消息数据
     * @return 是否发送成功
     */
    public static boolean sendMessage(Long userId, String eventName, Object data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter == null) {
            log.warn("未找到用户的SSE连接: userId={}", userId);
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
            return true;
        } catch (IOException e) {
            log.error("发送SSE消息失败: userId={}, error={}", userId, e.getMessage());
            removeEmitter(userId);
            return false;
        }
    }

    /**
     * 发送消息给指定用户（默认事件名为message）
     * @param userId 用户ID
     * @param data 消息数据
     * @return 是否发送成功
     */
    public static boolean sendMessage(Long userId, Object data) {
        return sendMessage(userId, "message", data);
    }

    /**
     * 发送完成信号
     * @param userId 用户ID
     * @return 是否发送成功
     */
    public static boolean sendComplete(Long userId) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter == null) {
            log.warn("未找到用户的SSE连接: userId={}", userId);
            return false;
        }

        try {
            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data("done"));
            emitter.complete();
            removeEmitter(userId);
            return true;
        } catch (IOException e) {
            log.error("发送完成信号失败: userId={}, error={}", userId, e.getMessage());
            removeEmitter(userId);
            return false;
        }
    }

    /**
     * 检查用户是否已连接
     * @param userId 用户ID
     * @return 是否已连接
     */
    public static boolean isConnected(Long userId) {
        return userEmitters.containsKey(userId);
    }

    private static Runnable onTimeoutCallback(Long userId){
        return () -> {
            log.info("SSE连接超时: userId={}", userId);
            removeEmitter(userId);
        };
    }
    
    private static Consumer<Throwable> onErrorCallback(Long userId){
        return (e) -> {
            log.error("SSE连接错误: userId={}, error={}", userId, e.getMessage());
            removeEmitter(userId);
        };
    }
    
    private static Runnable onCompletionCallback(Long userId){
        return () -> {
            log.info("SSE连接完成: userId={}", userId);
            removeEmitter(userId);
        };
    }

    /**
     * 移除指定用户的连接
     * @param userId 用户ID
     * @return 是否移除成功
     */
    public static boolean removeEmitter(Long userId){
        SseEmitter removed = userEmitters.remove(userId.toString());
        if (removed != null) {
            log.info("移除SSE连接: userId={}", userId);
            return true;
        }
        return false;
    }

    /**
     * 获取当前连接数
     * @return 连接数
     */
    public static int getConnectionCount() {
        return userEmitters.size();
    }
}
