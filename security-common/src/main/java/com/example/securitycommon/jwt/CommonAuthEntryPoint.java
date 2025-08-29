package com.example.securitycommon.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommonAuthEntryPoint implements AuthenticationEntryPoint {

    private final boolean respondJson;

    public CommonAuthEntryPoint(boolean respondJson) {
        this.respondJson = respondJson;
    }

    @Override
    public void commence(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         AuthenticationException authException) throws IOException {
        if (!respondJson) {
            httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Отсутствует авторизация");
            return;
        }
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> body = new HashMap<>();
        body.put("error", "UNAUTHORIZED");
        body.put("message", "Error: UNAUTHORIZED");
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        new ObjectMapper().writeValue(httpServletResponse.getWriter(), body);
    }
}
