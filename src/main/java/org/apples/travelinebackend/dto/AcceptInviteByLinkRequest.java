package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코드로 초대 수락 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptInviteByLinkRequest {

    @NotBlank(message = "초대 코드는 필수입니다.")
    private String code;
}
