package com.palatophil.common;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(ErrorCode.SUCCESS.getCode());
        r.setMessage(ErrorCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(ErrorCode code) {
        return fail(code, code.getMessage());
    }

    public static <T> Result<T> fail(ErrorCode code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code.getCode());
        r.setMessage(message);
        return r;
    }
}
