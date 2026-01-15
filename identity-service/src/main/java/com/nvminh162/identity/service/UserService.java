package com.nvminh162.identity.service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nvminh162.identity.dto.request.UserCreationRequest;
import com.nvminh162.identity.dto.request.UserUpdateRequest;
import com.nvminh162.identity.dto.response.UserResponse;
import com.nvminh162.identity.entity.User;
import com.nvminh162.identity.enums.Role;
import com.nvminh162.identity.exception.AppException;
import com.nvminh162.identity.exception.ErrorCode;
import com.nvminh162.identity.mapper.UserMapper;
import com.nvminh162.identity.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }
        User user = userMapper.toUser(request);
        
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());
        user.setRoles(roles);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    /* PreAuthorize: In charge trước khi method thực hiện => Kiểm tra quyền trước khi thực hiện method */
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    /* PostAuthorize: In charge sau khi method thực hiện => Kiểm tra quyền sau khi thực hiện method */
    /* Trong trường hợp này admin or lấy UserName nếu chính mình thì mới được phép lấy => còn lại không được phép */
    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public UserResponse getUser(String id) {
        return userMapper.toUserResponse(
                userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        log.info("Context: {}", context);
        String name = context.getAuthentication().getName();
        return userMapper.toUserResponse(userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
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
