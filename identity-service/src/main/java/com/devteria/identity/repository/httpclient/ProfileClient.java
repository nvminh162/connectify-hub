package com.devteria.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.devteria.identity.dto.request.ApiResponse;
import com.devteria.identity.dto.request.UserProfileCreationRequest;
import com.devteria.identity.dto.response.UserProfileResponse;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "profile-service", url = "${app.services.profile}")
public interface ProfileClient {

    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UserProfileCreationRequest request
    );
}
