package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPlacesRequest {
    
    @NotNull(message = "여행 계획 ID는 필수입니다.")
    private Long travelPlanId;
    
    @NotNull(message = "일차 번호는 필수입니다.")
    private Integer dayNumber;
    
    @NotEmpty(message = "장소 ID 목록은 필수입니다.")
    private List<Long> placeIds;  // 새로운 순서대로 장소 ID 배열
}


