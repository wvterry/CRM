package com.example.userservice.JWT;

import com.example.securitycommon.jwt.CommonAuthEntryPoint;
import com.example.securitycommon.jwt.CommonAuthTokenFilter;
import com.example.securitycommon.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    private final JwtUtil jwtUtil;

    @Autowired
    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Bean
    public CommonAuthTokenFilter authTokenFilter(JwtUtil jwtUtil,
                                                 @Value("${security.allow-internal-api-key:true}") boolean allowInternal,
                                                 @Value("${internal.api.key}") String internalApiKey,
                                                 @Value("${security.fail-fast.missing-auth:true}") boolean failMissing,
                                                 @Value("${security.fail-fast.invalid-jwt:true}") boolean failInvalid) {
        return new CommonAuthTokenFilter(jwtUtil, allowInternal, internalApiKey, failMissing, failInvalid);
    }

    @Bean
    public AuthenticationEntryPoint authEntryPoint(@Value("${security.entrypoint.json:false}") boolean respondJson) {
        return new CommonAuthEntryPoint(respondJson);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CommonAuthTokenFilter authTokenFilter,
                                                   AuthenticationEntryPoint authEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/user/**").hasAuthority("ROLE_INTERNAL")
                        .requestMatchers(HttpMethod.PUT, "/api/user/update/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/user/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(authTokenFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
