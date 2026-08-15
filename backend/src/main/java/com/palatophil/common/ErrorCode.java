package com.palatophil.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "成功"),
    BAD_REQUEST(40000, "请求参数错误"),
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    FORBIDDEN(40300, "权限不足"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "资源冲突"),
    INTERNAL_ERROR(50000, "服务器内部错误"),
    WX_LOGIN_FAILED(50001, "微信登录失败"),
    INVALID_CREDENTIALS(40101, "账号或密码错误"),
    SESSION_FULL(40901, "会话人数已满"),
    INGREDIENT_REQUIRED(40001, "至少提交 1 个食材"),
    AMOUNT_TOO_SMALL(40002, "食材克数必须大于 0");

    private final int code;
    private final String message;
}
