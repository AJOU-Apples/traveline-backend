package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.PhotoVisibility;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderPhotosRequest {
    
    private Long placeId;  // 특정 장소의 사진 순서 변경 (optional)
    private Long travelPlanId;  // 여행 계획의 사진 순서 변경 (optional)
    private Integer dayNumber;  // 특정 날짜의 사진 순서 변경 (optional)
    
    @NotNull(message = "공개 설정은 필수입니다.")
    private PhotoVisibility visibility;  // 공개 설정 (PERSONAL / SHARED)
    
    @NotEmpty(message = "사진 ID 목록은 필수입니다.")
    private List<Long> photoIds;  // 새로운 순서대로 사진 ID 배열
}

