package com.nvminh162.profile_service.service;

import org.springframework.stereotype.Service;

import com.nvminh162.profile_service.dto.reponse.UserProfileResponse;
import com.nvminh162.profile_service.dto.request.UserProfileCreationRequest;
import com.nvminh162.profile_service.entity.UserProfile;
import com.nvminh162.profile_service.mapper.UserProfileMapper;
import com.nvminh162.profile_service.repository.UserProfileRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
public class UserProfileService {
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    public UserProfileResponse createUserProfile(UserProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toUserProfile(request);
        userProfile = userProfileRepository.save(userProfile);
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public UserProfileResponse getUserProfile(String id) {
        UserProfile userProfile =
                userProfileRepository.findById(id).orElseThrow(() -> new RuntimeException("User profile not found"));
        return userProfileMapper.toUserProfileResponse(userProfile);
    }

    public List<UserProfileResponse> getAllProfiles() {
        var profiles = userProfileRepository.findAll();
        return profiles.stream().map(userProfileMapper::toUserProfileResponse).toList();
    }
}
