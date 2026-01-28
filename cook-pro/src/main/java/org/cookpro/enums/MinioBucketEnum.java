package org.cookpro.enums;

public enum MinioBucketEnum {


    PUBLIC("public", "公共存储桶"),


    ;
    public final String value;
    public final String description;

    MinioBucketEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
