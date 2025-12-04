package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 초대 코드 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteLinkDto {
    private String code;
    private LocalDateTime expiresAt;
}

