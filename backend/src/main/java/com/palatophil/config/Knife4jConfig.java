package com.palatophil.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Knife4jConfig implements WebMvcConfigurer {

    @Bean
    public OpenAPI palatophilOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("无定珍 API 文档")
                .description("全栈开发规格说明书 V1.2 - M1 基础框架")
                .version("1.0.0"));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}