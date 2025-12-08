package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.ReportService;
import org.apples.travelinebackend.service.TravelPostService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travel-posts")
@RequiredArgsConstructor
public class TravelPostController {

    private final TravelPostService travelPostService;
    private final ReportService reportService;

    /**
     * 여행기 생성
     */
    @PostMapping
    public ResponseEntity<TravelPostDto> createTravelPost(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateTravelPostRequest request) {
        TravelPostDto createdPost = travelPostService.createTravelPost(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    /**
     * 여행기 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<TravelPostDto>> getTravelPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Long authorId) {
        Page<TravelPostDto> posts = travelPostService.getTravelPosts(page, size, sort, authorId);
        return ResponseEntity.ok(posts);
    }

    /**
     * 여행기 상세 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<TravelPostDto> getTravelPost(
            @PathVariable Long postId) {
        // 인증 정보가 있으면 사용, 없으면 null
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            currentUser = (User) authentication.getPrincipal();
        }
        Long userId = currentUser != null ? currentUser.getId() : null;
        TravelPostDto post = travelPostService.getTravelPostById(postId, userId);
        return ResponseEntity.ok(post);
    }

    /**
     * 링크로 여행기 조회 (인증 불필요)
     */
    @GetMapping("/share/{shareCode}")
    public ResponseEntity<TravelPostDto> getTravelPostByShareCode(
            @PathVariable String shareCode) {
        TravelPostDto post = travelPostService.getTravelPostByShareCode(shareCode);
        return ResponseEntity.ok(post);
    }

    /**
     * 여행기 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<TravelPostDto> updateTravelPost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateTravelPostRequest request) {
        TravelPostDto updatedPost = travelPostService.updateTravelPost(postId, request, currentUser);
        return ResponseEntity.ok(updatedPost);
    }

    /**
     * 여행기 삭제
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteTravelPost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId) {
        travelPostService.deleteTravelPost(postId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * 좋아요 추가/취소
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId) {
        LikeResponse response = travelPostService.toggleLike(postId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 생성
     */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<CommentDto> createComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentDto comment = travelPostService.createComment(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    /**
     * 댓글 목록 조회
     */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<Page<CommentDto>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<CommentDto> comments = travelPostService.getComments(postId, page, size);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        CommentDto updatedComment = travelPostService.updateComment(postId, commentId, request, currentUser);
        return ResponseEntity.ok(updatedComment);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        travelPostService.deleteComment(postId, commentId, currentUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 좋아요 추가/취소
     */
    @PostMapping("/{postId}/comments/{commentId}/like")
    public ResponseEntity<LikeResponse> toggleCommentLike(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        LikeResponse response = travelPostService.toggleCommentLike(postId, commentId, currentUser);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 여행기 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<Page<TravelPostDto>> getMyTravelPosts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TravelPostDto> posts = travelPostService.getMyTravelPosts(currentUser.getId(), page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * 좋아요한 여행기 목록 조회
     */
    @GetMapping("/liked")
    public ResponseEntity<Page<TravelPostDto>> getLikedTravelPosts(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TravelPostDto> posts = travelPostService.getLikedTravelPosts(currentUser.getId(), page, size);
        return ResponseEntity.ok(posts);
    }

    /**
     * 여행기 신고
     */
    @PostMapping("/{postId}/reports")
    public ResponseEntity<ReportResponse> reportPost(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.reportPost(postId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 댓글 신고
     */
    @PostMapping("/{postId}/comments/{commentId}/reports")
    public ResponseEntity<ReportResponse> reportComment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportService.reportComment(postId, commentId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

