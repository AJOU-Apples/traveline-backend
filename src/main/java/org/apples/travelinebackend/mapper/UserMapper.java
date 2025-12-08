package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.UserDto;
import org.apples.travelinebackend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    /**
     * User Entity를 UserDto로 변환
     */
    public UserDto toDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .username(entity.getNickname())
                .profileImage(entity.getProfileImage())
                .bio(entity.getBio())
                .isActive(entity.getIsActive())
                .isVerified(entity.getIsVerified())
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}

