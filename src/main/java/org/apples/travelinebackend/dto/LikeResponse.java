package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {
    private Boolean isLiked;
    private Integer likeCount;
    
    @Builder.Default
    private List<Long> likedBy = new ArrayList<>(); // 좋아요한 멤버 ID 목록
}

