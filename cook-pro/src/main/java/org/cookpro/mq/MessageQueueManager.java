package org.cookpro.mq;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于阻塞队列的消息队列管理器
 */
public class MessageQueueManager {
    // 每个用户对应一个阻塞队列，存储待发送的消息
    private static final Map<Long, BlockingQueue<SSEQueueMessage>> USER_MESSAGE_QUEUE = new ConcurrentHashMap<>();
    // 队列最大容量（可根据业务调整）
    private static final int QUEUE_CAPACITY = 1000;

    /**
     * 向指定用户的队列中添加消息
     */
    public static void addMessageToQueue(SSEQueueMessage message) throws InterruptedException {
        Long toId = message.getToId();
        // 不存在则创建队列，ConcurrentHashMap保证线程安全
        BlockingQueue<SSEQueueMessage> queue = USER_MESSAGE_QUEUE.computeIfAbsent(toId, k -> new ArrayBlockingQueue<>(QUEUE_CAPACITY));
        // 阻塞添加（队列满时等待），也可使用offer非阻塞（根据业务选择）
        queue.put(message);
    }

    /**
     * 消费指定用户队列中的所有积压消息
     * @param toId 接收者ID
     * @param emitter 用户的SSE发射器
     */
    public static void consumeMessageQueue(Long toId, SseEmitter emitter) {
        BlockingQueue<SSEQueueMessage> queue = USER_MESSAGE_QUEUE.get(toId);
        if (queue == null || queue.isEmpty()) {
            return;
        }

        // 循环消费队列中的消息（非阻塞，避免长时间占用线程）
        SSEQueueMessage message;
        while ((message = queue.poll()) != null) {
            try {
                // 发送积压的消息
                emitter.send(SseEmitter.event()
                        .name(message.getEventName())
                        .data(message.getData()));
            } catch (IOException e) {
                // 发送失败可重新入队（需避免死循环），或记录日志
                e.printStackTrace();
                try {
                    queue.put(message); // 重新入队，后续重试
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 用户断开连接时移除队列（可选，避免内存泄漏）
     */
    public static void removeQueue(Long toId) {
        USER_MESSAGE_QUEUE.remove(toId);
    }
}