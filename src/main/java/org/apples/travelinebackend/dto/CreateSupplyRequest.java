package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSupplyRequest {

    @NotNull(message = "여행 계획 ID는 필수입니다")
    private Long travelPlanId;

    @NotBlank(message = "준비물 이름은 필수입니다")
    private String text;

    private Integer quantity;
    private String unit;
    private String category;
    private String memo;
}

