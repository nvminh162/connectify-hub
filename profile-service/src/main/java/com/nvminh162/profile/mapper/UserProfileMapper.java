package com.nvminh162.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.nvminh162.profile.dto.request.ProfileCreationRequest;
import com.nvminh162.profile.dto.request.UpdateProfileRequest;
import com.nvminh162.profile.dto.response.UserProfileResponse;
import com.nvminh162.profile.entity.UserProfile;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {
    UserProfile toUserProfile(ProfileCreationRequest request);

    UserProfileResponse toUserProfileResponse(UserProfile entity);

    void update(@MappingTarget UserProfile entity, UpdateProfileRequest request);
}
