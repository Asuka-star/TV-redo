package com.wenjia.common.advice;


import com.wenjia.common.exception.BaseException;
import com.wenjia.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;
import java.util.stream.Collectors;


@RestControllerAdvice
@Slf4j
public class CommonExceptionAdvice {

    @ExceptionHandler(BaseException.class)
    public Object handleBadRequestException(BaseException e) {
        log.error("自定义异常 -> {} , 异常原因：{}  ",e.getClass().getName(), e.getMessage());
        log.debug("", e);
        return processResponse(e);
    }

    @ExceptionHandler(RpcException.class)
    public ResponseEntity<Result<Void>> handleRpcException(RpcException e) {
        // 关键：从RpcException中解出自定义异常
        Throwable cause = e.getCause();

        if (cause instanceof BaseException) {
            BaseException ce = (BaseException) cause;
            // 返回自定义异常中的业务错误信息
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.error(ce.getMessage()));
        }

        // 处理其他类型的RpcException
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error("远程服务调用失败: " + e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getAllErrors()
                .stream().map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("|"));
        log.error("请求参数校验异常 -> {}", msg);
        log.debug("", e);
        return processResponse(new BaseException(msg));
    }
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException e) {
        log.error("请求参数绑定异常 ->BindException， {}", e.getMessage());
        log.debug("", e);
        return processResponse(new BaseException("请求参数格式错误"));
    }

    private ResponseEntity<Result<Void>> processResponse(BaseException e){
        return ResponseEntity.status(500).body(Result.error(e.getMessage()));
    }
}