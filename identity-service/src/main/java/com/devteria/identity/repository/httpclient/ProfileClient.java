package com.devteria.identity.repository.httpclient;

import com.devteria.identity.configuration.AuthenticationRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.devteria.identity.dto.request.ApiResponse;
import com.devteria.identity.dto.request.UserProfileCreationRequest;
import com.devteria.identity.dto.response.UserProfileResponse;

/*
* Thêm tham số thứ 3: configuration = { AuthenticationRequestInterceptor.class } nếu không dùng @Component => cách này Best Practice
*/
@FeignClient(name = "profile-service", url = "${app.services.profile}", configuration = { AuthenticationRequestInterceptor.class })
public interface ProfileClient {

    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(
            @RequestBody UserProfileCreationRequest request
    );
}
