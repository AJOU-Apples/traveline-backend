package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.MemberRole;

/**
 * 멤버 역할 변경 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemberRoleRequest {
    
    @NotNull(message = "역할은 필수입니다.")
    private MemberRole role;
}

