package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    // 부분 업데이트 지원: 모든 필드 선택사항
    private String text;
    private LocalDate deadline;
    private String memo;
    private Boolean checked;
}

