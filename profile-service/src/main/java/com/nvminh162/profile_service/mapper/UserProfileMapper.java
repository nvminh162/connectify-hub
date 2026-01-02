package com.nvminh162.profile_service.mapper;

import org.mapstruct.Mapper;

import com.nvminh162.profile_service.dto.reponse.UserProfileResponse;
import com.nvminh162.profile_service.dto.request.UserProfileCreationRequest;
import com.nvminh162.profile_service.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(UserProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile userProfile);
}
