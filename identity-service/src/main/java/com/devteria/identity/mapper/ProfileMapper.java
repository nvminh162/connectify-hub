package com.devteria.identity.mapper;

import org.mapstruct.Mapper;

import com.devteria.identity.dto.request.UserCreationRequest;
import com.devteria.identity.dto.request.UserProfileCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    UserProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
