package org.cookpro.enums;

public enum ChatRecordTypeEnum {

    USER("用户消息"),
    ASSISTANT("助手回复"),
    TOOL_CALL("工具调用开始"),
    TOOL_RESULT("工具调用结束"),
    AUDIT("审核信息"),
    ASSISTANT_FINAL("助手回复-最终")

    ;
    public  final String description;

    ChatRecordTypeEnum(String description) {
        this.description = description;
    }

    public static ChatRecordTypeEnum fromDescription(String description){
        for (ChatRecordTypeEnum value : ChatRecordTypeEnum.values()) {
            if (value.description.equals(description)){
                return value;
            }
        }
        throw new IllegalArgumentException("类型不存在");
    }
}
