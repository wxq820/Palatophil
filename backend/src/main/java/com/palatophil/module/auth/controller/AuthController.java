package com.palatophil.module.auth.controller;

import com.palatophil.common.Result;
import com.palatophil.module.auth.dto.AdminLoginRequest;
import com.palatophil.module.auth.dto.LoginResponse;
import com.palatophil.module.auth.dto.WxLoginRequest;
import com.palatophil.module.auth.service.AuthService;
import com.palatophil.security.JwtAuthFilter;
import com.palatophil.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "鉴权")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "微信小程序登录")
    @PostMapping("/wx-login")
    public Result<LoginResponse> wxLogin(@Valid @RequestBody WxLoginRequest req) {
        return Result.ok(authService.wxLogin(req));
    }

    @Operation(summary = "PC 后台账号密码登录")
    @PostMapping("/admin-login")
    public Result<LoginResponse> adminLogin(@Valid @RequestBody AdminLoginRequest req) {
        return Result.ok(authService.adminLogin(req));
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<LoginResponse> me(HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) request.getAttribute(JwtAuthFilter.USER_ATTR);
        if (loginUser == null) {
            return Result.ok(null);
        }
        return Result.ok(authService.currentUser(loginUser.getUserId()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok();
    }
}
