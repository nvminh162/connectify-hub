package com.nvminh162.identity.configuration;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // options: có thể có hoặc không có
public class SecurityConfig {

    @Value("${jwt.signer-key}")
    private String SIGNER_KEY;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> {
            request
                    .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll()
                    .anyRequest().authenticated();
        });

        http.oauth2ResourceServer(oauth2 -> // Đăng ký với provider manager support jwt token => khi thực hiện request cung cấp token vào header Authorization: Bearer <token> => jwt authentication thực hiện authentication dựa trên token
            oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())) // options: .jwkSetUri(null): cấu hình với resource server thứ3 cần url này => dùng decoder để decode token do hệ thống generate token
        );

        http.csrf(AbstractHttpConfigurer::disable); // spring security mặc định sẽ bật cấu hình csrf => Bảo vệ endpoint trước attack cross-site request forgery => config để disable csrf

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
        return NimbusJwtDecoder
            .withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS512)
            .build();
    }
}
