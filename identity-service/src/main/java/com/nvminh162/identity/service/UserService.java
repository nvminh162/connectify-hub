package com.nvminh162.identity.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nvminh162.identity.dto.request.UserCreationRequest;
import com.nvminh162.identity.dto.request.UserUpdateRequest;
import com.nvminh162.identity.dto.response.UserResponse;
import com.nvminh162.identity.entity.User;
import com.nvminh162.identity.exception.AppException;
import com.nvminh162.identity.exception.ErrorCode;
import com.nvminh162.identity.mapper.UserMapper;
import com.nvminh162.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User user = userMapper.toUser(request);
        return userRepository.save(user);
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
