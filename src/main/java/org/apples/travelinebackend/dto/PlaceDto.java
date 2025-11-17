package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceDto {
    
    private Long id;
    private Long travelPlanId;
    private Long travelDayId;
    private Integer dayNumber;
    
    // 장소 정보
    private String name;
    private String address;
    
    // 위치 정보
    private Double latitude;
    private Double longitude;
    private String placeId;  // Google Place ID
    
    // 방문 정보
    private String time;
    private Integer orderIndex;
    
    // 메모
    private String memo;  // 공유 메모
    private Map<String, String> personalMemos;  // 개인 메모
    
    // 방문 상태
    private Boolean isVisited;
    private LocalDateTime visitedAt;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
}


