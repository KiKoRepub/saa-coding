package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.entity.ChatRecord;
import org.cookpro.service.ChatRecordService;
import org.cookpro.vo.ChatRecordInfoVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat/record")
public class ChatRecordController {

    @Resource
    private ChatRecordService chatRecordService;


    @GetMapping("/userList")
    @Operation(summary = "获取用户聊天历史", description = "根据用户ID获取聊天历史记录")
    public R<List<ChatRecord>> getChatHistory(@RequestParam("userId") Long userId) {
        List<ChatRecord> history = chatRecordService.listByUserId(userId);
        return R.ok(history);
    }

    @GetMapping("/conversationList")
    @Operation(summary = "获取会话聊天历史", description = "根据会话ID获取聊天历史记录")
    public R<List<ChatRecordInfoVo>> getChatHistoryByConversationId(@RequestParam("conversationId") String conversationId) {
        return R.ok(chatRecordService.listByConversationId(conversationId));
    }
}
