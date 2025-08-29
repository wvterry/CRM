package com.example.securitycommon.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


public class CommonAuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final boolean allowInternalApiKey;
    private final String validApiKey;
    private final boolean failFastOnMissingAuth;
    private final boolean failFastOnInvalidJwt;


    public CommonAuthTokenFilter(JwtUtil jwtUtil,
                                 boolean allowInternalApiKey,
                                 String validApiKey,
                                 boolean failFastOnMissingAuth,
                                 boolean failFastOnInvalidJwt) {
        this.jwtUtil = jwtUtil;
        this.allowInternalApiKey = allowInternalApiKey;
        this.validApiKey = validApiKey;
        this.failFastOnMissingAuth = failFastOnMissingAuth;
        this.failFastOnInvalidJwt = failFastOnInvalidJwt;
    }

    public void doFilterInternal(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse,
                                 FilterChain filterChain) throws ServletException, IOException {
        if (allowInternalApiKey) {
            String apiKey = httpServletRequest.getHeader("X-API-KEY");
            if (apiKey != null && apiKey.equals(validApiKey)) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
        }

        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (failFastOnMissingAuth) {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Отсутствуют учетные данные аутентификации");
                return;
            } else {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            if (failFastOnInvalidJwt) {
                httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Некорректный токен");
                return;
            } else {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
                return;
            }
        }

        String subject = jwtUtil.getEmailFromToken(token);
        List<GrantedAuthority> authorities = jwtUtil.getAuthorities(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(subject, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
