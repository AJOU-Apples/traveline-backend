package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.MemberRole;

/**
 * 멤버 초대 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {
    
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotNull(message = "이메일은 필수입니다.")
    private String email;
    
    @NotNull(message = "역할은 필수입니다.")
    private MemberRole role;
}

