package org.cookpro.node;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BaseGraphNode {
    @Schema(description = "节点ID", example = "node-1")
    private String nodeId;
    @Schema(description = "下一个节点id", example = "node-2")
    private String nextNodeId;
    @Schema(description = "条件表达式")
    private String nextCondition;
    @Schema(description = "节点名称", example = "工具调用节点")
    private String name;
    @Schema(description = "节点描述", example = "这是一个用于调用工具的节点")
    private String description;
}
