package com.palatophil.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private long expiresIn;
    private Long userId;
    private String openid;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String loginType;
}
