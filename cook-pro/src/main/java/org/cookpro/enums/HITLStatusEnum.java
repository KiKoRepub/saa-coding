package org.cookpro.enums;


public enum HITLStatusEnum {
    WAITING("等待审核"),
    PROCESSING("审核中"),
    APPROVED("审核通过"),
    EDITED("已修改"),
    REJECTED("已拒绝"),
    ;

    public final String description;


     HITLStatusEnum(String description) {
        this.description = description;
    }

    public static HITLStatusEnum fromDescription(String description){
        for (HITLStatusEnum value : HITLStatusEnum.values()) {
            if (value.description.equals(description)){
                return value;
            }
        }
        throw new IllegalArgumentException("类型不存在");
    }
}
