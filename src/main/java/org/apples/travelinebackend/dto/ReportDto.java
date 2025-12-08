package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ReportReason;
import org.apples.travelinebackend.entity.ReportStatus;
import org.apples.travelinebackend.entity.ReportTargetType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private Long reporterId;
    private String reporterUsername;
    private ReportTargetType reportedContentType;  // API 응답에서는 reportedContentType으로 명시
    private Long reportedContentId;  // API 응답에서는 reportedContentId로 명시
    private ReportReason reason;
    private String description;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 신고 대상 정보
    private Object reportedContent;  // TravelPostDto 또는 CommentDto
    
    // 내부 사용용 (Mapper에서 사용)
    @Builder.Default
    private ReportTargetType targetType = null;  // 내부 변환용
    
    @Builder.Default
    private Long targetId = null;  // 내부 변환용
}

