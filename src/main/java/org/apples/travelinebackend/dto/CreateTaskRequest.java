package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {

    @NotNull(message = "여행 계획 ID는 필수입니다")
    private Long travelPlanId;

    @NotBlank(message = "작업 내용은 필수입니다")
    private String text;

    private LocalDate deadline;
    private String memo;
}

