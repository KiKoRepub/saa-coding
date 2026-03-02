package org.cookpro.node.refer;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RobustNode implements NodeAction {
    private static final Logger logger = LoggerFactory.getLogger(RobustNode.class);

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        try {
// 参数验证
            String input = (String) state.value("input")
                    .orElseThrow(() -> new IllegalArgumentException("Missing 'input' in state"));

            logger.info("Processing input: {}", input);

// 业务逻辑
            String result = processInput(input);

// 返回结果
            Map<String, Object> output = new HashMap<>();
            output.put("output", result);
            return output;

        } catch (Exception e) {
            logger.error("Error in RobustNode", e);
// 返回错误信息而不是抛出异常
            Map<String, Object> errorOutput = new HashMap<>();
            errorOutput.put("error", e.getMessage());
            return errorOutput;
        }
    }


    private String processInput(String input) {
// 具体业务逻辑
        return input;
    }
}