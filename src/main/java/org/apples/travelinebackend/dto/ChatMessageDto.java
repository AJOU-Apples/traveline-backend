package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private Long travelPlanId;
    private AuthUserDto user;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

