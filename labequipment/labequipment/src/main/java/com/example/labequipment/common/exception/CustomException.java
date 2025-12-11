package com.example.labequipment.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class CustomException extends RuntimeException {
    private Integer code;
    private String message;

    public CustomException(String message) {
        this.code = 500; // 默认为 500
        this.message = message;
    }

    public CustomException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}