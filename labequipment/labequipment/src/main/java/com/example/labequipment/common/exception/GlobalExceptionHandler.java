package com.example.labequipment.common.exception;

import com.example.labequipment.common.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 捕获我们自定义的业务异常
    @ExceptionHandler(CustomException.class)
    public Result<String> handleCustomException(CustomException e) {
        // 返回 code=500 (或自定义code), msg="密码错误", data=null
        return Result.error(e.getCode(), e.getMessage());
    }

    // 捕获其他未知的系统异常
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        e.printStackTrace(); // 打印堆栈方便排查
        return Result.error(500, "系统未知错误: " + e.getMessage());
    }
}