package org.cookpro.enums;

public enum SSEEventEnum {

    WAITING_AUDIT("等待审核"),
    AUDIT_PASSED("审核通过"),
    AUDIT_REJECTED("审核拒绝"),
    AUDIT_EDITED("审核编辑"),
    DELETED("已删除"),
    FORBIDDEN("已禁止"),
    COMPLETED("已完成"),
    ERROR("错误")
    ;

    public final String eventName;

    SSEEventEnum(String eventName) {
        this.eventName = eventName;
    }

    public static SSEEventEnum fromEventName(String eventName) {
        for (SSEEventEnum event : SSEEventEnum.values()) {
            if (event.eventName.equals(eventName)) {
                return event;
            }
        }
        return null;
    }

}
