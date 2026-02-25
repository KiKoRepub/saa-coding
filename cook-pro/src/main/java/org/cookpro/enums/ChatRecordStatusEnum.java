package org.cookpro.enums;

public enum ChatRecordStatusEnum {

    NON_REVIEW("无需审核"),
    REVIEWING("待审核"),
    APPROVED("审核通过"),
    REJECTED("审核拒绝"),
    EDITED("审核编辑"),

    ;

    public final String description;


    ChatRecordStatusEnum(String description) {
        this.description = description;
    }


    public static ChatRecordStatusEnum fromDescription(String description) {
        for (ChatRecordStatusEnum status : ChatRecordStatusEnum.values()) {
            if (status.name().equalsIgnoreCase(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant with description: " + description + "");
    }
}
