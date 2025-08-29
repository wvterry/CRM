package com.example.demo.JWT;

import com.example.securitycommon.jwt.CommonAuthEntryPoint;
import com.example.securitycommon.jwt.CommonAuthTokenFilter;
import com.example.securitycommon.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;

    @Autowired
    public WebSecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Bean
    public CommonAuthTokenFilter authTokenFilter(JwtUtil jwtUtil,
                                                 @Value("${security.allow-internal-api-key:false}") boolean allowInternal,
                                                 @Value("${internal.api.key:}") String internalApiKey,
                                                 @Value("${security.fail-fast.missing-auth:false}") boolean failMissing,
                                                 @Value("${security.fail-fast.invalid-jwt:false}") boolean failInvalid) {
        return new CommonAuthTokenFilter(jwtUtil, allowInternal, internalApiKey, failMissing, failInvalid);
    }

    @Bean
    public AuthenticationManager authenticationManager
            (AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            @Value("${security.entrypoint.json:true}") boolean respondJson) {
        return new CommonAuthEntryPoint(respondJson);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
                                                   CommonAuthTokenFilter authTokenFilter,
                                                   AuthenticationEntryPoint authEntryPoint) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(authEntryPoint))
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/api/auth/signup", "/api/auth/signin").permitAll()
                        .anyRequest().authenticated()
                );
        httpSecurity.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

}
