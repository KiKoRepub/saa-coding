package org.cookpro.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler{




    @ExceptionHandler(ChatException.class)
    public String handleChatException(ChatException ex) {
        // Log the exception or perform other actions as needed

        log.error("ChatException occurred: {}", ex.getMessage());




        return ex.getMessage();
    }


}
