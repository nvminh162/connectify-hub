package com.nvminh162.api_gateway.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nvminh162.api_gateway.dto.request.ApiResponse;
import com.nvminh162.api_gateway.service.IdentityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    String[] publicEndpoints = {
            "/identity/auth/.*", //regex
            "/identity/users/registration",
            "/notification/email/send"
    };

    @Value("${app.api-prefix}")
    @NonFinal
    String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Enter authentication filter");

        if (isPublicEndpoint(exchange.getRequest()))
             return chain.filter(exchange);

        // Get token from authorization header
        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader)) {
            return unauthenticated(exchange.getResponse());
        }
        // Verify token (delegate identity service)
        // !OK => 401, 503, disconnect, ...
        // OK => continue
        String token = authHeader.getFirst().replace("Bearer ", "");
        log.info("Token: {}", token);

        /*
         * subscribe()
         * Dùng để TIÊU THỤ dữ liệu
         * Kết thúc pipeline
         * Không dùng trong filter/controller
         * */
        identityService.introspect(token).subscribe(introspectResponseApiResponse -> {
            log.info("Result introspect boolean: {}", introspectResponseApiResponse.getResult().isValid());
        });

        /*
         * flatMap()
         * Dùng để xử lý dữ liệu và TIẾP TỤC pipeline
         * Trả về Mono / Flux
         * Dùng trong WebFlux filter / controller
         * */
        return identityService.introspect(token).flatMap(introspectResponseApiResponse -> {
            if (!introspectResponseApiResponse.getResult().isValid()) { // block 401
                return unauthenticated(exchange.getResponse());
            } else {
                return chain.filter(exchange);
            }
        }).onErrorResume(throwable -> unauthenticated(exchange.getResponse())); // block 503, disconnect, ...
    }

    @Override
    public int getOrder() {
        return -1; // số càng nhỏ thứ tự càng lớn
    }

    private Mono<Void> unauthenticated(ServerHttpResponse response) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(1401)
                .result("Unauthenticated")

                .build();

        String body = null;
        try {
            body = objectMapper.writeValueAsString(apiResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints).anyMatch(s -> request.getURI().getPath().matches(apiPrefix + s));
    }
}
