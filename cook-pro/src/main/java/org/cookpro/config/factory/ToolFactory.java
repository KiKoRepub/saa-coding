package org.cookpro.config.factory;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cookpro.config.properties.ToolEnvProperties;
import org.cookpro.dto.ToolChatDTO;
import org.cookpro.enums.ToolSourceEnum;
import org.cookpro.tools.AgenticRAGSearchTool;
import org.cookpro.tools.WebSearchTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.support.ToolDefinitions;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
@Slf4j
@Component
public class ToolFactory {

    @Resource
    WebSearchTool webSearchTool;
    @Resource
    VectorStore vectorStore;




    public  List<ToolCallback> selectTools(List<ToolChatDTO> toolEntities) {

        List<ToolCallback> result = new LinkedList<>();
        for (ToolChatDTO toolEntity : toolEntities) {
            if (ToolSourceEnum.IN_PROJECT.description.equals(toolEntity.getSource())){
                // 内置工具
                // 根据工具名获取工具实例
                ToolCallback[] toolCallback = getTool(toolEntity.getToolName());
                result.addAll(Arrays.stream(toolCallback).toList());
            }
            if (ToolSourceEnum.OUTSIDE.description.equals(toolEntity.getSource())){
                // 外部自定义工具
                System.out.println("外部自定义工具，暂不支持 ==> 工具名: "+toolEntity.getToolName());
                // TODO 后续支持外部自定义工具，提供接口让用户上传工具的jar包，或者提供接口让用户编写工具的代码
            }
        }

        return result;
    }
    private ToolCallback[] getTool(String toolName){
        Object tool = null;

        if (Objects.equals(toolName, WebSearchTool.class.getName())){
            tool = webSearchTool;
        }
        else if (Objects.equals(toolName, AgenticRAGSearchTool.class.getName())){
            tool = new AgenticRAGSearchTool(vectorStore);
        }
        // 如果工具名不是工具类的全限定名，则尝试在工具类中查找方法名匹配的工具
        if (tool == null) {
            return   tryGetFromMethod(toolName);
        }

        return ToolCallbacks.from(tool);
    }

    private ToolCallback[] tryGetFromMethod(String toolName) {
        Object resultClass = null;
        Method result = null;
        Method toolInWebSearch = findMethodByName(WebSearchTool.class, toolName);


        if (toolInWebSearch != null) {
            result = toolInWebSearch;
                resultClass = webSearchTool;
        }

        if (result == null) {
            Method toolInRAGSearch = findMethodByName(AgenticRAGSearchTool.class, toolName);
            if (toolInRAGSearch != null) {
                result = toolInRAGSearch;
                resultClass = new AgenticRAGSearchTool(vectorStore);
            }
        }

        if (result == null) {
            log.warn("未找到工具: " + toolName + "，请检查工具类中是否存在该工具，并且工具名是否正确");
            return null;
        }else {
            MethodToolCallback resultTool = MethodToolCallback.builder()
                    .toolDefinition(ToolDefinitions.from(result))
                    .toolMetadata(ToolMetadata.from(result))
                    .toolMethod(result)
                    .toolObject(resultClass)
                    .toolCallResultConverter(ToolUtils.getToolCallResultConverter(result))
                    .build();
            return new MethodToolCallback[]{
                    resultTool
            };

        }

    }

    private static Method findMethodByName(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        log.warn("未找到工具方法: "+methodName+"，请检查工具类 "+clazz.getName()+" 中是否存在该方法，并且方法名是否正确");
        return null;
    }


}
