package com.nvminh162.identity.configuration;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // options: có thể có hoặc không có
@EnableMethodSecurity
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
                    /* Cách 1: Default is SCOPE_ => custom jwtAuthenticationConverter() => ROLE_ */
                    // .requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN") /* ROLE_ADMIN */
                    /* Cách 2: hasRole(Role.ADMIN.name()) => Role.ADMIN.name() là enum Role của admin */
                    // .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name()) /* ADMIN */
                    /* Cách 3: Phân quyền trên method */
                    .anyRequest().authenticated();
        });

        /*
        Đăng ký với provider manager support jwt token 
        => khi thực hiện request cung cấp token vào header Authorization: Bearer <token>
        => jwt authentication thực hiện authentication dựa trên token
        */
        http.oauth2ResourceServer(oauth2 ->
            oauth2
                /* options: .jwkSetUri(null): cấu hình với resource server thứ3 cần url này
                => dùng decoder để decode token do hệ thống generate token */
                .jwt(jwtConfigurer -> jwtConfigurer
                    .decoder(jwtDecoder())
                    /* Converter tùy chỉnh để map scope thành authorities */
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

        /*
        spring security mặc định sẽ bật cấu hình csrf
        => Bảo vệ endpoint trước attack cross-site request forgery
        => config để disable csrf */
        http.csrf(AbstractHttpConfigurer::disable);

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

    /*
    Default is 10
    Càng lốn độ mạnh mật khẩu càng cao
    => ảnh hưởng performance nếu đặt lớn
    => yêu cầu mã hoá dưới 1s tuỳ yêu cầu system */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /* Default is SCOPE_ */
    /* Cần để map scope thành authorities */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
