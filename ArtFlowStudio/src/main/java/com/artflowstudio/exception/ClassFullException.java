package com.artflowstudio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ClassFullException extends RuntimeException {
    public ClassFullException(String message) {
        super(message);
    }
}