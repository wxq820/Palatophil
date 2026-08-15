package com.palatophil.module.auth.wx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WxClient {

    private final WxProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String JSCODE2SESSION =
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    /**
     * 通过 code 换取 OpenID。
     * Mock 模式：直接基于 code 计算 MD5 当作 OpenID，方便本地联调。
     */
    public String code2Openid(String code) {
        if (props.isMockMode()) {
            return mockOpenid(code);
        }
        String url = String.format(JSCODE2SESSION, props.getAppid(), props.getSecret(), code);
        try {
            WxSessionResponse resp = restTemplate.getForObject(url, WxSessionResponse.class);
            if (resp == null || resp.getOpenid() == null) {
                throw new IllegalStateException("微信返回为空");
            }
            return resp.getOpenid();
        } catch (Exception e) {
            log.warn("wx.jscode2session 失败, 降级 mock: {}", e.getMessage());
            return mockOpenid(code);
        }
    }

    private String mockOpenid(String code) {
        if (code == null || code.isBlank()) {
            code = UUID.randomUUID().toString();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(code.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return "mock_" + sb.substring(0, 24);
        } catch (Exception e) {
            return "mock_" + UUID.randomUUID().toString().replace("-", "");
        }
    }
}
