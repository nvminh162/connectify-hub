package com.nvminh162.chat.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nvminh162.chat.dto.ApiResponse;
import com.nvminh162.chat.dto.request.IntrospectRequest;
import com.nvminh162.chat.dto.response.IntrospectResponse;

@FeignClient(name = "identity-client", url = "${app.services.identity.url}")
public interface IdentityClient {
    @PostMapping("/auth/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
