package com.htv.oauth2.mapper;

import com.htv.oauth2.domain.*;
import com.htv.oauth2.dto.request.user.RegisterRequest;
import com.htv.oauth2.dto.request.user.UserUpdateRequest;
import com.htv.oauth2.dto.response.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual implementation of UserMapper
 * Alternative to MapStruct if annotation processing causes issues
 */
@ApplicationScoped
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .mfaEnabled(user.getMfaEnabled())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) return List.of();
        return users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public User fromRegisterRequest(RegisterRequest request) {
        if (request == null) return null;

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .emailVerified(false)
                .mfaEnabled(false)
                .build();
    }

    public void updateUserFromRequest(UserUpdateRequest request, User user) {
        if (request == null || user == null) return;

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
    }
}

