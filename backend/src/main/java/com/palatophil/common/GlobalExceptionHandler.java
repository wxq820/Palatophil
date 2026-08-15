package com.palatophil.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final java.util.Map<Integer, Integer> CODE_TO_HTTP = new java.util.HashMap<>() {{
        put(ErrorCode.BAD_REQUEST.getCode(), 400);
        put(ErrorCode.UNAUTHORIZED.getCode(), 401);
        put(ErrorCode.INVALID_CREDENTIALS.getCode(), 401);
        put(ErrorCode.FORBIDDEN.getCode(), 403);
        put(ErrorCode.NOT_FOUND.getCode(), 404);
        put(ErrorCode.CONFLICT.getCode(), 409);
        put(ErrorCode.SESSION_FULL.getCode(), 409);
        put(ErrorCode.INGREDIENT_REQUIRED.getCode(), 400);
        put(ErrorCode.AMOUNT_TOO_SMALL.getCode(), 400);
        put(ErrorCode.WX_LOGIN_FAILED.getCode(), 500);
        put(ErrorCode.INTERNAL_ERROR.getCode(), 500);
    }};

    @ExceptionHandler(BizException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleBiz(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        int http = CODE_TO_HTTP.getOrDefault(e.getErrorCode().getCode(), 400);
        return org.springframework.http.ResponseEntity
                .status(http)
                .body(Result.fail(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return org.springframework.http.ResponseEntity
                .status(400)
                .body(Result.fail(ErrorCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(BindException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleBind(BindException e) {
        return org.springframework.http.ResponseEntity
                .status(400)
                .body(Result.fail(ErrorCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleReadable(HttpMessageNotReadableException e) {
        return org.springframework.http.ResponseEntity
                .status(400)
                .body(Result.fail(ErrorCode.BAD_REQUEST, "请求体不可读"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleDup(DuplicateKeyException e) {
        log.warn("唯一键冲突: {}", e.getMessage());
        return org.springframework.http.ResponseEntity
                .status(409)
                .body(Result.fail(ErrorCode.CONFLICT, "数据已存在，请勿重复添加"));
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleAll(Exception e) {
        log.error("系统异常", e);
        return org.springframework.http.ResponseEntity
                .status(500)
                .body(Result.fail(ErrorCode.INTERNAL_ERROR, e.getMessage()));
    }
}
