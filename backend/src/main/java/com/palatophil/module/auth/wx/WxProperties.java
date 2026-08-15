package com.palatophil.module.auth.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "palatophil.wx")
public class WxProperties {

    private String appid;
    private String secret;
    private boolean mockMode = true;
}
