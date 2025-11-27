package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.AuthUserDto;
import org.apples.travelinebackend.dto.ChatMessageDto;
import org.apples.travelinebackend.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessageMapper {

    public ChatMessageDto toDto(ChatMessage chatMessage) {
        if (chatMessage == null) {
            return null;
        }

        return ChatMessageDto.builder()
                .id(chatMessage.getId())
                .travelPlanId(chatMessage.getTravelPlan().getId())
                .user(AuthUserDto.builder()
                        .id(chatMessage.getUser().getId())
                        .email(chatMessage.getUser().getEmail())
                        .name(chatMessage.getUser().getName())
                        .username(chatMessage.getUser().getNickname())
                        .profileImageUrl(chatMessage.getUser().getProfileImage())
                        .build())
                .message(chatMessage.getMessage())
                .createdAt(chatMessage.getCreatedAt())
                .updatedAt(chatMessage.getUpdatedAt())
                .build();
    }
}

