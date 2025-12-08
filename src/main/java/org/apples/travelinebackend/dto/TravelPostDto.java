package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ExpenseDisplayType;
import org.apples.travelinebackend.entity.MemoDisplayType;
import org.apples.travelinebackend.entity.PostVisibility;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelPostDto {

    private Long id;
    private Long travelPlanId;
    private UserDto author;
    private String title;
    private String content;
    private String coverImageUrl;

    // 공유 설정
    private PostVisibility visibility;
    private String shareCode;

    // 표기 범위 설정
    private ExpenseDisplayType expenseDisplayType;
    private MemoDisplayType memoDisplayType;

    // 통계
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isLiked;

    // 여행 계획 정보 (상세 조회 시에만 포함)
    private TravelPlanDto travelPlan;

    // 사진 목록
    @Builder.Default
    private List<PhotoDto> photos = new ArrayList<>();

    // 댓글 목록 (상세 조회 시에만 포함)
    @Builder.Default
    private List<CommentDto> comments = new ArrayList<>();

    // 메타데이터
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

