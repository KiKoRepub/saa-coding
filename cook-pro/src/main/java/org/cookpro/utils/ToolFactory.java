package org.cookpro.utils;

import jakarta.annotation.Resource;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.entity.ToolEntity;
import org.cookpro.tools.AgenticRAGSearchTool;
import org.cookpro.tools.WebSearchTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ToolFactory {

    @Resource
    ToolEnvProperties properties;
    @Resource
    VectorStore vectorStore;

    public ToolCallback[] getTool(String toolName){
        Object tool = null;

        if (Objects.equals(toolName, WebSearchTool.class.getName())){
            tool = new WebSearchTool(properties.getGoogleWebSearchApiKey());
        }
        else if (Objects.equals(toolName, AgenticRAGSearchTool.class.getName())){
            tool = new AgenticRAGSearchTool(vectorStore);
        }

        return ToolCallbacks.from(tool);
    }


    public  List<ToolCallback> selectTools(List<ToolEntity> toolEntities) {

        List<ToolCallback> result = new LinkedList<>();
        for (ToolEntity toolEntity : toolEntities) {
            if (toolEntity.getSource() == 0){
                // 内置工具
                // 根据工具名获取工具实例
                ToolCallback[] toolCallback = getTool(toolEntity.getToolName());
                result.addAll(Arrays.stream(toolCallback).toList());
            }
            if (toolEntity.getSource() == 1){
                // 外部自定义工具
                System.out.println("外部自定义工具，暂不支持 ==> 工具名: "+toolEntity.getToolName());
                // TODO 后续支持外部自定义工具，提供接口让用户上传工具的jar包，或者提供接口让用户编写工具的代码
            }
        }

        return result;
    }
}
