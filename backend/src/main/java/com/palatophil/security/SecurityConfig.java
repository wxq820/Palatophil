package com.palatophil.security;

import com.palatophil.common.ErrorCode;
import com.palatophil.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public static final String[] PUBLIC_PATHS = new String[]{
            "/api/auth/**",
            "/api/health",
            "/doc.html",
            "/doc.html/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/v3/api-docs/**",
            "/favicon.ico",
            "/error"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(c -> c.disable())
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/ingredients", "/api/ingredients/*",
                                "/api/recipes", "/api/recipes/*",
                                "/api/recipe-tags").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(this::onUnauthenticated)
                        .accessDeniedHandler(this::onForbidden))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void onUnauthenticated(HttpServletRequest req, HttpServletResponse res, Exception e) throws java.io.IOException {
        writeJson(res, 401, Result.fail(ErrorCode.UNAUTHORIZED, "请先登录"));
    }

    private void onForbidden(HttpServletRequest req, HttpServletResponse res, Exception e) throws java.io.IOException {
        writeJson(res, 403, Result.fail(ErrorCode.FORBIDDEN, "无权限访问"));
    }

    private void writeJson(HttpServletResponse res, int status, Object body) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=utf-8");
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(res.getOutputStream(), body);
    }
}
