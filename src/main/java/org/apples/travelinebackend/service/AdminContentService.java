package org.apples.travelinebackend.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.ContentHideResponse;
import org.apples.travelinebackend.dto.HideContentRequest;
import org.apples.travelinebackend.dto.ProcessReportRequest;
import org.apples.travelinebackend.dto.ReportDto;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TravelPostMapper;
import org.apples.travelinebackend.mapper.UserMapper;
import org.apples.travelinebackend.repository.ReportRepository;
import org.apples.travelinebackend.repository.TravelPostCommentRepository;
import org.apples.travelinebackend.repository.TravelPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminContentService {

    private final TravelPostRepository travelPostRepository;
    private final TravelPostCommentRepository travelPostCommentRepository;
    private final ReportRepository reportRepository;
    private final TravelPostMapper travelPostMapper;
    private final UserMapper userMapper;
    private final EntityManager entityManager;

    /**
     * 콘텐츠 비공개 처리
     */
    @Transactional
    public ContentHideResponse hideContent(String contentType, Long contentId, HideContentRequest request, User admin) {
        if ("travel-posts".equals(contentType)) {
            // 관리자용: 숨겨진 여행기도 조회 가능 (@SQLRestriction 우회)
            TravelPost post = findPostByIdIncludingHidden(contentId)
                    .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", contentId));
            
            post.setIsHidden(request.getHide());
            TravelPost savedPost = travelPostRepository.save(post);
            
            log.info("여행기 숨김 처리: postId={}, hide={}, reason={}, adminId={}", 
                    contentId, request.getHide(), request.getReason(), admin.getId());
            
            return ContentHideResponse.builder()
                    .id(savedPost.getId())
                    .isHidden(savedPost.getIsHidden())
                    .hiddenAt(LocalDateTime.now())
                    .hiddenBy(userMapper.toDto(admin))
                    .build();
                    
        } else if ("comments".equals(contentType)) {
            // 관리자용: 숨겨진 댓글도 조회 가능 (@SQLRestriction 우회)
            TravelPostComment comment = findCommentByIdIncludingHidden(contentId)
                    .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", contentId));
            
            comment.setIsHidden(request.getHide());
            TravelPostComment savedComment = travelPostCommentRepository.save(comment);
            
            log.info("댓글 숨김 처리: commentId={}, hide={}, reason={}, adminId={}", 
                    contentId, request.getHide(), request.getReason(), admin.getId());
            
            return ContentHideResponse.builder()
                    .id(savedComment.getId())
                    .isHidden(savedComment.getIsHidden())
                    .hiddenAt(LocalDateTime.now())
                    .hiddenBy(userMapper.toDto(admin))
                    .build();
        } else {
            throw new BadRequestException("지원하지 않는 콘텐츠 타입입니다: " + contentType);
        }
    }

    /**
     * 콘텐츠 공개 처리 (숨김 해제)
     */
    @Transactional
    public ContentHideResponse unhideContent(String contentType, Long contentId, User admin) {
        HideContentRequest request = HideContentRequest.builder()
                .hide(false)
                .reason("관리자에 의한 공개 처리")
                .build();
        return hideContent(contentType, contentId, request, admin);
    }

    /**
     * 신고 처리 완료
     */
    @Transactional
    public ReportDto processReport(Long reportId, ProcessReportRequest request, User admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고", "id", reportId));

        if (request.getAction() == ProcessReportRequest.ProcessAction.HIDE) {
            // 콘텐츠 숨김 처리
            ReportTargetType targetType = report.getTargetType();
            String contentType = targetType == ReportTargetType.POST ? "travel-posts" : "comments";
            
            HideContentRequest hideRequest = HideContentRequest.builder()
                    .hide(true)
                    .reason(request.getReason())
                    .reportId(reportId)
                    .build();
            
            hideContent(contentType, report.getTargetId(), hideRequest, admin);
            report.setStatus(ReportStatus.REVIEWED);
        } else if (request.getAction() == ProcessReportRequest.ProcessAction.REJECT) {
            // 신고 거절
            report.setStatus(ReportStatus.REJECTED);
        }
        
        Report savedReport = reportRepository.save(report);
        
        log.info("신고 처리 완료: reportId={}, action={}, reason={}, adminId={}", 
                reportId, request.getAction(), request.getReason(), admin.getId());

        // ReportDto 변환 (신고 대상 정보 포함)
        ReportDto dto = ReportDto.builder()
                .id(savedReport.getId())
                .reporterId(savedReport.getReporter().getId())
                .reporterUsername(savedReport.getReporter().getUsername())
                .reportedContentType(savedReport.getTargetType())
                .reportedContentId(savedReport.getTargetId())
                .reason(savedReport.getReason())
                .description(savedReport.getDescription())
                .status(savedReport.getStatus())
                .createdAt(savedReport.getCreatedAt())
                .updatedAt(savedReport.getUpdatedAt())
                .build();
        
        // 신고 대상 정보 포함 (숨겨진 콘텐츠도 조회 가능)
        if (savedReport.getTargetType() == ReportTargetType.POST) {
            findPostByIdIncludingHidden(savedReport.getTargetId())
                    .ifPresent(post -> dto.setReportedContent(travelPostMapper.toDto(post, null)));
        } else if (savedReport.getTargetType() == ReportTargetType.COMMENT) {
            findCommentByIdIncludingHidden(savedReport.getTargetId())
                    .ifPresent(comment -> dto.setReportedContent(travelPostMapper.toCommentDto(comment, null)));
        }
        
        return dto;
    }

    /**
     * 관리자용: 숨겨진 여행기도 조회 가능 (@SQLRestriction 우회 - Native Query 사용)
     */
    @SuppressWarnings("unchecked")
    private Optional<TravelPost> findPostByIdIncludingHidden(Long id) {
        // Native Query로 @SQLRestriction 완전히 우회
        jakarta.persistence.Query nativeQuery = entityManager.createNativeQuery(
            "SELECT * FROM travel_posts WHERE id = :id AND deleted_at IS NULL",
            TravelPost.class
        );
        nativeQuery.setParameter("id", id);
        
        List<?> results = nativeQuery.getResultList();
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        TravelPost post = (TravelPost) results.get(0);
        return Optional.of(post);
    }

    /**
     * 관리자용: 숨겨진 댓글도 조회 가능 (@SQLRestriction 우회 - Native Query 사용)
     */
    @SuppressWarnings("unchecked")
    private Optional<TravelPostComment> findCommentByIdIncludingHidden(Long id) {
        // Native Query로 @SQLRestriction 완전히 우회
        jakarta.persistence.Query nativeQuery = entityManager.createNativeQuery(
            "SELECT * FROM travel_post_comments WHERE id = :id AND deleted_at IS NULL",
            TravelPostComment.class
        );
        nativeQuery.setParameter("id", id);
        
        List<?> results = nativeQuery.getResultList();
        
        if (results.isEmpty()) {
            return Optional.empty();
        }
        
        TravelPostComment comment = (TravelPostComment) results.get(0);
        return Optional.of(comment);
    }
}

