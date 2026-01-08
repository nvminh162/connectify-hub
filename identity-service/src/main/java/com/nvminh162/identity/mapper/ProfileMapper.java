package com.nvminh162.identity.mapper;

import org.mapstruct.Mapper;

import com.nvminh162.identity.dto.request.ProfileCreationRequest;
import com.nvminh162.identity.dto.request.UserCreationRequest;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileCreationRequest toProfileCreationRequest(UserCreationRequest request);
}
