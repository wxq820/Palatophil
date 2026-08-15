package com.palatophil.module.auth.service;

import com.palatophil.common.BizException;
import com.palatophil.common.ErrorCode;
import com.palatophil.module.auth.dto.AdminLoginRequest;
import com.palatophil.module.auth.dto.LoginResponse;
import com.palatophil.module.auth.dto.WxLoginRequest;
import com.palatophil.module.auth.wx.WxClient;
import com.palatophil.module.user.entity.SysUser;
import com.palatophil.module.user.service.SysUserService;
import com.palatophil.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WxClient wxClient;
    private final SysUserService userService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse wxLogin(WxLoginRequest req) {
        String openid = wxClient.code2Openid(req.getCode());
        if (openid == null) {
            throw new BizException(ErrorCode.WX_LOGIN_FAILED);
        }
        SysUser user = userService.findByOpenid(openid);
        if (user == null) {
            user = userService.createWxUser(openid, req.getNickname());
        }
        userService.touchLastLogin(user.getId());

        String token = tokenProvider.issueToken(user.getId(), user.getRole(), "WX");
        return buildResponse(token, user, "WX");
    }

    public LoginResponse adminLogin(AdminLoginRequest req) {
        SysUser user = userService.findByUsername(req.getUsername());
        if (user == null || user.getPassword() == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new BizException(ErrorCode.FORBIDDEN, "非管理员账号");
        }
        userService.touchLastLogin(user.getId());

        String token = tokenProvider.issueToken(user.getId(), user.getRole(), "ADMIN");
        return buildResponse(token, user, "ADMIN");
    }

    public LoginResponse currentUser(Long uid) {
        SysUser user = userService.findById(uid);
        if (user == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return LoginResponse.builder()
                .userId(user.getId())
                .openid(user.getOpenid())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .loginType("ADMIN".equals(user.getRole()) ? "ADMIN" : "WX")
                .build();
    }

    private LoginResponse buildResponse(String token, SysUser user, String loginType) {
        return LoginResponse.builder()
                .token(token)
                .expiresIn(tokenProvider.getExpireSeconds())
                .userId(user.getId())
                .openid(user.getOpenid())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .loginType(loginType)
                .build();
    }
}
