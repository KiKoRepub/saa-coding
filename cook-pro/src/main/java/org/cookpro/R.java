package org.cookpro;

import lombok.Data;


public class R <T> {

    private final T data;
    private final int code;




    public static <T> R<T> error(T data) {
        return new R<>(data,500);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(data,200);
    }

    public R(T data, int code) {
        this.data = data;
        this.code = code;
    }

}
