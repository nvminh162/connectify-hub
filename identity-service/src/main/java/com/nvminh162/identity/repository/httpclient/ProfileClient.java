package com.nvminh162.identity.repository.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nvminh162.identity.configuration.AuthenticationRequestInterceptor;
import com.nvminh162.identity.dto.request.ApiResponse;
import com.nvminh162.identity.dto.request.UserProfileCreationRequest;
import com.nvminh162.identity.dto.response.UserProfileResponse;

/*
 * Thêm tham số thứ 3: configuration = { AuthenticationRequestInterceptor.class } nếu không dùng @Component => cách này Best Practice
 */
@FeignClient(
        name = "profile-service",
        url = "${app.services.profile}",
        configuration = {AuthenticationRequestInterceptor.class})
public interface ProfileClient {

    @PostMapping(value = "/internal/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<UserProfileResponse> createProfile(@RequestBody UserProfileCreationRequest request);
}
