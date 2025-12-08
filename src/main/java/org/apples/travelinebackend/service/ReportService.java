package org.apples.travelinebackend.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TravelPostMapper;
import org.apples.travelinebackend.repository.ReportRepository;
import org.apples.travelinebackend.repository.TravelPostCommentRepository;
import org.apples.travelinebackend.repository.TravelPostRepository;
import org.springframework.data.domain.Page;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final TravelPostRepository travelPostRepository;
    private final TravelPostCommentRepository travelPostCommentRepository;
    private final TravelPostMapper travelPostMapper;
    private final EntityManager entityManager;

    /**
     * 여행기 신고
     */
    @Transactional
    public ReportResponse reportPost(Long postId, CreateReportRequest request, User reporter) {
        // 1. 여행기 존재 확인
        TravelPost post = travelPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 2. 본인 게시글 신고 방지
        if (post.getAuthor().getId().equals(reporter.getId())) {
            throw new BadRequestException("본인의 게시글은 신고할 수 없습니다.");
        }

        // 3. 중복 신고 체크
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), ReportTargetType.POST, postId)) {
            throw new BadRequestException("이미 신고한 게시글입니다.");
        }

        // 4. 신고 생성
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(ReportTargetType.POST)
                .targetId(postId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);
        
        log.info("여행기 신고 생성: reportId={}, postId={}, reporterId={}, reason={}",
                savedReport.getId(), postId, reporter.getId(), request.getReason());

        return ReportResponse.builder()
                .id(savedReport.getId())
                .message("신고가 접수되었습니다.")
                .build();
    }

    /**
     * 댓글 신고
     */
    @Transactional
    public ReportResponse reportComment(Long postId, Long commentId, CreateReportRequest request, User reporter) {
        // 1. 댓글 존재 확인 및 여행기 소속 확인
        TravelPostComment comment = travelPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", commentId));

        if (!comment.getTravelPost().getId().equals(postId)) {
            throw new BadRequestException("해당 여행기의 댓글이 아닙니다.");
        }

        // 2. 본인 댓글 신고 방지
        if (comment.getUser().getId().equals(reporter.getId())) {
            throw new BadRequestException("본인의 댓글은 신고할 수 없습니다.");
        }

        // 3. 중복 신고 체크
        if (reportRepository.existsByReporterIdAndTargetTypeAndTargetId(
                reporter.getId(), ReportTargetType.COMMENT, commentId)) {
            throw new BadRequestException("이미 신고한 댓글입니다.");
        }

        // 4. 신고 생성
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(ReportTargetType.COMMENT)
                .targetId(commentId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        Report savedReport = reportRepository.save(report);
        
        log.info("댓글 신고 생성: reportId={}, commentId={}, reporterId={}, reason={}",
                savedReport.getId(), commentId, reporter.getId(), request.getReason());

        return ReportResponse.builder()
                .id(savedReport.getId())
                .message("신고가 접수되었습니다.")
                .build();
    }

    /**
     * 관리자: 신고 목록 조회
     */
    public Page<ReportDto> getReports(int page, int size, String status, ReportTargetType contentType) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports;
        
        // status가 "PROCESSED"인 경우 REVIEWED와 REJECTED 모두 포함
        if ("PROCESSED".equals(status)) {
            reports = reportRepository.findProcessedReports(contentType, pageable);
        } else {
            ReportStatus reportStatus = status != null ? ReportStatus.valueOf(status) : null;
            reports = reportRepository.findByFilters(reportStatus, contentType, pageable);
        }
        
        return reports.map(report -> {
            ReportDto dto = ReportDto.builder()
                    .id(report.getId())
                    .reporterId(report.getReporter().getId())
                    .reporterUsername(report.getReporter().getUsername())
                    .reportedContentType(report.getTargetType())
                    .reportedContentId(report.getTargetId())
                    .reason(report.getReason())
                    .description(report.getDescription())
                    .status(report.getStatus())
                    .createdAt(report.getCreatedAt())
                    .updatedAt(report.getUpdatedAt())
                    .build();
            
            // 신고 대상 정보 포함 (숨겨진 콘텐츠도 조회 가능)
            if (report.getTargetType() == ReportTargetType.POST) {
                findPostByIdIncludingHidden(report.getTargetId())
                        .ifPresent(post -> dto.setReportedContent(travelPostMapper.toDto(post, null)));
            } else if (report.getTargetType() == ReportTargetType.COMMENT) {
                findCommentByIdIncludingHidden(report.getTargetId())
                        .ifPresent(comment -> dto.setReportedContent(travelPostMapper.toCommentDto(comment, null)));
            }
            
            return dto;
        });
    }

    /**
     * 관리자: 신고 상세 조회
     */
    public ReportDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고", "id", reportId));
        
        ReportDto dto = ReportDto.builder()
                .id(report.getId())
                .reporterId(report.getReporter().getId())
                .reporterUsername(report.getReporter().getUsername())
                .reportedContentType(report.getTargetType())
                .reportedContentId(report.getTargetId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
        
        // 신고 대상 정보 포함 (숨겨진 콘텐츠도 조회 가능)
        if (report.getTargetType() == ReportTargetType.POST) {
            findPostByIdIncludingHidden(report.getTargetId()).ifPresent(post -> {
                dto.setReportedContent(travelPostMapper.toDto(post, null));
            });
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            findCommentByIdIncludingHidden(report.getTargetId()).ifPresent(comment -> {
                dto.setReportedContent(travelPostMapper.toCommentDto(comment, null));
            });
        }
        
        return dto;
    }

    /**
     * 관리자: 신고 상태 변경
     */
    @Transactional
    public ReportDto updateReportStatus(Long reportId, UpdateReportStatusRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고", "id", reportId));
        
        report.setStatus(request.getStatus());
        reportRepository.save(report);
        
        log.info("신고 상태 변경: reportId={}, status={}", reportId, request.getStatus());
        
        return getReport(reportId);
    }

    /**
     * 관리자: 신고된 콘텐츠 숨김 처리
     */
    @Transactional
    public void hideReportedContent(Long reportId, HideContentRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("신고", "id", reportId));
        
        if (report.getTargetType() == ReportTargetType.POST) {
            // 관리자용: 숨겨진 여행기도 조회 가능 (@SQLRestriction 우회)
            TravelPost post = findPostByIdIncludingHidden(report.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", report.getTargetId()));
            post.setIsHidden(request.getHide());
            travelPostRepository.save(post);
            log.info("여행기 숨김 처리: postId={}, hide={}", report.getTargetId(), request.getHide());
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            // 관리자용: 숨겨진 댓글도 조회 가능 (@SQLRestriction 우회)
            TravelPostComment comment = findCommentByIdIncludingHidden(report.getTargetId())
                    .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", report.getTargetId()));
            comment.setIsHidden(request.getHide());
            travelPostCommentRepository.save(comment);
            log.info("댓글 숨김 처리: commentId={}, hide={}", report.getTargetId(), request.getHide());
        }
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

