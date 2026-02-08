package org.cookpro.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.cookpro.AgentBackground;
import org.cookpro.R;
import org.cookpro.dto.UserChattingDTO;
import org.cookpro.exception.ChatException;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/chat")
@Tag(name = "聊天接口")
public class ChatController {


    @Resource
    DashScopeChatModel chatModel;


    @GetMapping("/chat")
    @Operation(summary = "与烹饪助手聊天", description = "向烹饪助手发送消息，获取回复")
    public R<String> chat(@RequestParam("message") String message){

        AgentBackground cookingAssistant = AgentBackground.COOKING_ASSISTANT;

        ReactAgent agent = ReactAgent.builder()
                .name(cookingAssistant.name())
                .model(chatModel)
                .outputType(String.class)
                .systemPrompt(cookingAssistant.systemPrompt)
                .build();

        try {
            AssistantMessage call = agent.call(message);

            return R.ok(call.getText());
        }catch (GraphRunnerException e){
            throw  new ChatException("聊天失败: " + e.getMessage(), e);
        }
    }

    @PostMapping("/chatMore")
    @Operation(summary = "与烹饪助手进行功能更多的聊天", description = "向烹饪助手发送消息列表，获取回复")
    public R<String> chat(@RequestBody UserChattingDTO dto){
        return R.ok("功能更多的聊天接口暂未实现，敬请期待！");
    }


}
