package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.sse.SSEServer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/sse")
@Tag(name = "SSE连接管理")
public class SSEController {

    /**
     * 建立SSE连接
     * @param userId 用户ID（可以是conversationId或其他唯一标识）
     * @return SseEmitter对象
     */
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "建立SSE连接", description = "客户端通过此接口建立SSE长连接，用于接收实时消息")
    public SseEmitter connect(@RequestParam("userId") Long userId) {
        log.info("收到SSE连接请求: userId={}", userId);

        // 如果已存在连接，先移除
        if (SSEServer.isConnected(userId)) {
            log.info("用户已存在连接，先移除旧连接: userId={}", userId);
            SSEServer.removeEmitter(userId);
        }

        SseEmitter emitter = SSEServer.connect(userId);

        if (emitter != null) {
            // 发送连接成功消息
            try {
                emitter.send(SseEmitter.event()
                        .name("connected")
                        .data("SSE连接建立成功"));
            } catch (Exception e) {
                log.error("发送连接成功消息失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        return emitter;
    }

    /**
     * 断开SSE连接
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/disconnect")
    @Operation(summary = "断开SSE连接", description = "主动断开指定用户的SSE连接")
    public String disconnect(@RequestParam("userId") Long userId) {
        log.info("收到断开SSE连接请求: userId={}", userId);

        boolean success = SSEServer.removeEmitter(userId);

        if (success) {
            return "SSE连接已断开: " + userId;
        } else {
            return "未找到SSE连接: " + userId;
        }
    }

    /**
     * 检查连接状态
     * @param userId 用户ID
     * @return 连接状态
     */
    @GetMapping("/status")
    @Operation(summary = "检查连接状态", description = "查询指定用户的SSE连接状态")
    public String checkStatus(@RequestParam("userId") Long userId) {
        boolean connected = SSEServer.isConnected(userId);
        return String.format("用户 %s 的连接状态: %s", userId, connected ? "已连接" : "未连接");
    }

    /**
     * 获取当前连接数
     * @return 连接数统计
     */
    @GetMapping("/count")
    @Operation(summary = "获取连接数", description = "获取当前活跃的SSE连接总数")
    public String getConnectionCount() {
        int count = SSEServer.getConnectionCount();
        return String.format("当前活跃连接数: %d", count);
    }
}