package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.PhotoVisibility;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhotoDto {
    
    private Long id;
    private Long travelPlanId;
    private Long travelDayId;
    private Integer dayNumber;
    private Long placeId;
    private Long userId;
    private String username;
    
    // 파일 정보
    private String uri;
    private String thumbnailUri;
    private String filename;
    private Long fileSize;
    private String mimeType;
    
    // 이미지 정보
    private Integer width;
    private Integer height;
    
    // 위치 정보
    private Double latitude;
    private Double longitude;
    
    // 메타데이터
    private LocalDateTime timestamp;
    private LocalDateTime uploadedAt;
    
    // 공개 설정
    private PhotoVisibility visibility;
    
    // 캡션
    private String caption;
    
    // 순서
    private Integer orderIndex;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

