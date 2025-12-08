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
public class CreatePlaceRequest {
    
    @NotNull(message = "여행 계획 ID는 필수입니다.")
    private Long travelPlanId;
    
    @NotNull(message = "일차 번호는 필수입니다.")
    private Integer dayNumber;
    
    @NotBlank(message = "장소 이름은 필수입니다.")
    private String name;
    
    private String address;
    private Double latitude;
    private Double longitude;
    private String placeId;  // Google Place ID
    private String time;     // "10:00"
    private String memo;     // 공유 메모
}


