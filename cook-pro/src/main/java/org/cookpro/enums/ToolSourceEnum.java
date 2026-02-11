package org.cookpro.enums;

public enum ToolSourceEnum {

    IN_PROJECT("内置工具"),
    MCP_TOOL("mcp 导入"),
    OUTSIDE("外部工具"),

    ;
    public final String description;

     ToolSourceEnum(String description) {
        this.description = description;
    }


    public static ToolSourceEnum fromDescription(String description){
        for (ToolSourceEnum value : ToolSourceEnum.values()) {
            if (value.description.equals(description)){
                return value;
            }
        }
        throw new IllegalArgumentException("类型不存在");
    }

}
