package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.InvitationStatus;
import org.apples.travelinebackend.entity.MemberRole;

import java.time.LocalDateTime;

/**
 * 여행 계획 멤버 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {
    
    private Long id;
    private Long userId;
    private String username;
    private String name;
    private String email;
    private String profileImage;
    private MemberRole role;
    private InvitationStatus status;
    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private String invitedByName;  // 초대한 사람 이름
}

