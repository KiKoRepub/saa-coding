package org.cookpro.exception;

import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler{




    @ExceptionHandler(ChatException.class)
    public String handleChatException(ChatException ex) {

        log.error("ChatException occurred: {}", ex.getMessage());

        return ex.getMessage();
    }

    @ExceptionHandler(SaTokenException.class)
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }



}
