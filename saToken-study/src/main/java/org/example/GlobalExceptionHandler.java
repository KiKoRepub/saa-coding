package org.example;

import cn.dev33.satoken.exception.NotLoginException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NotLoginException.class)
    public String handlerException(NotLoginException e) {
        e.printStackTrace();
        return "请先登录";
    }



}
