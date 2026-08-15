package com.palatophil.security;

import com.palatophil.common.ErrorCode;
import com.palatophil.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final JwtProperties props;

    public static final String USER_ATTR = "palatophil.user";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(props.getHeader());
        if (header != null && header.startsWith(props.getPrefix())) {
            String token = header.substring(props.getPrefix().length()).trim();
            try {
                Claims claims = tokenProvider.parse(token);
                Long userId = claims.get(JwtTokenProvider.CLAIM_USER_ID, Long.class);
                String role = claims.get(JwtTokenProvider.CLAIM_ROLE, String.class);
                String loginType = claims.get(JwtTokenProvider.CLAIM_LOGIN_TYPE, String.class);

                LoginUser loginUser = new LoginUser(userId, role, loginType);
                request.setAttribute(USER_ATTR, loginUser);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        loginUser, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException e) {
                writeUnauthorized(response, "Token 解析失败: " + e.getMessage());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=utf-8");
        Result<Void> body = Result.fail(ErrorCode.UNAUTHORIZED, msg);
        new com.fasterxml.jackson.databind.ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
