package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.PhotoVisibility;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePhotoRequest {
    
    private String caption;
    private PhotoVisibility visibility;
    private Long placeId;  // 사진을 특정 장소에 연결
}

