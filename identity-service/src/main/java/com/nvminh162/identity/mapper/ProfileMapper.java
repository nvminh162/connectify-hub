package com.nvminh162.identity.mapper;

import org.mapstruct.Mapper;

import com.nvminh162.identity.dto.request.UserCreationRequest;
import com.nvminh162.identity.dto.request.UserProfileCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    UserProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
