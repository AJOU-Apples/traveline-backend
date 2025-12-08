package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HideContentRequest {
    @NotNull(message = "숨김 여부는 필수입니다")
    private Boolean hide;

    private String reason;  // 숨김 사유
    private Long reportId;  // 관련 신고 ID (선택)
}
