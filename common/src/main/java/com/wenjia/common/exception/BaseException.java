package com.wenjia.common.exception;

import java.io.Serializable;

public class BaseException extends RuntimeException implements Serializable {
    public BaseException(String message) {
        super(message);
    }
}
