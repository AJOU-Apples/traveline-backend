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
public class ProcessReportRequest {
    @NotNull(message = "처리 액션은 필수입니다")
    private ProcessAction action;

    private String reason;  // 처리 사유

    public enum ProcessAction {
        HIDE,   // 숨김 처리
        REJECT  // 거절
    }
}

