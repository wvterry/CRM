package com.example.userservice.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${internal.api.key}")
    private String validApiKey;

    public AuthTokenFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

//        if (request.getRequestURI().startsWith("/api/internal/")) {
//            handleInternalRequest(request, response, filterChain);
//            return;
//        }
//
//        handleJwtRequest(request, response, filterChain);

        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey != null && apiKey.equals(validApiKey)) {
            setInternalAuthentication(request);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Проверка JWT для внешних вызовов
        final String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            handleJwtToken(authHeader.substring(7), request, response, filterChain);
            return;
        }

        // 3. Если нет ни API ключа, ни JWT
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing authentication credentials");

    }

    private void handleJwtToken(String token,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
            throws IOException, ServletException {

        if (!jwtUtil.validateToken(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            return;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private void setInternalAuthentication(HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

//    private void handleInternalRequest(HttpServletRequest request,
//                                       HttpServletResponse response,
//                                       FilterChain filterChain)
//            throws IOException, ServletException {
//
//        String apiKey = request.getHeader("X-API-KEY");
//
//        if (apiKey == null || !apiKey.equals(validApiKey)) {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
//            return;
//        }
//
//        setInternalAuthentication(request);
//        filterChain.doFilter(request, response);
//    }
//
//    private void handleJwtRequest(HttpServletRequest request,
//                                  HttpServletResponse response,
//                                  FilterChain filterChain)
//            throws IOException, ServletException {
//
//        final String authHeader = request.getHeader("Authorization");
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        final String token = authHeader.substring(7);
//
//        if (!jwtUtil.validateToken(token)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String username = jwtUtil.getUsernameFromToken(token);
//        List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(token);
//
//        UsernamePasswordAuthenticationToken authentication =
//                new UsernamePasswordAuthenticationToken(
//                        username,
//                        null,
//                        authorities
//                );
//
//        authentication.setDetails(
//                new WebAuthenticationDetailsSource().buildDetails(request)
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        filterChain.doFilter(request, response);
//    }
//
//    private void setInternalAuthentication(HttpServletRequest request) {
//        UsernamePasswordAuthenticationToken auth =
//                new UsernamePasswordAuthenticationToken(
//                        "internal-service",
//                        null,
//                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
//                );
//        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(auth);
//    }
}
