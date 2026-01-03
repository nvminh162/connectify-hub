package com.nvminh162.api_gateway.repository;

import com.nvminh162.api_gateway.dto.request.ApiResponse;
import com.nvminh162.api_gateway.dto.request.IntrospectRequest;
import com.nvminh162.api_gateway.dto.response.IntrospectResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;


public interface IdentityClient {
    @PostExchange(value = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE) // dùng PostExchange vì đây là Http của spring chứ không phải feign client như cách gọi của Identity tới profile service;
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);
}
