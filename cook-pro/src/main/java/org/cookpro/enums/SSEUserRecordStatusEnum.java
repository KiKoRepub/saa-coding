package org.cookpro.enums;

public enum SSEUserRecordStatusEnum {


    WAITING("等待中"),
    SUBMITTED("已提交"),
    DESTROYED("已销毁"),
    EXPIRED("已过期"),
    FORBIDDEN("已封禁"),
    ;

    public final String description;

    SSEUserRecordStatusEnum(String description) {
        this.description = description;
    }

    public static SSEUserRecordStatusEnum fromDescription(String description) {
        for (SSEUserRecordStatusEnum value : SSEUserRecordStatusEnum.values()) {
            if (value.description.equals(description)) {
                return value;
            }
        }
        throw new IllegalArgumentException("No enum constant with description " + description);
    }
}
