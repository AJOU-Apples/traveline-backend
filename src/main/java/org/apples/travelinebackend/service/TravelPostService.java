package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TravelPostMapper;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPostService {

    private final TravelPostRepository travelPostRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final PhotoRepository photoRepository;
    private final ExpenseRepository expenseRepository;
    private final MemoRepository memoRepository;
    private final TravelPostPhotoRepository travelPostPhotoRepository;
    private final TravelPostLikeRepository travelPostLikeRepository;
    private final TravelPostCommentRepository travelPostCommentRepository;
    private final TravelPostCommentLikeRepository travelPostCommentLikeRepository;
    private final TravelPostMapper travelPostMapper;
    private final TravelPlanMapper travelPlanMapper;

    /**
     * 여행기 생성
     */
    @Transactional
    public TravelPostDto createTravelPost(CreateTravelPostRequest request, User author) {
        // 1. TravelPlan 존재 여부 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", request.getTravelPlanId()));

        if (!travelPlan.hasAccess(author.getId())) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // 2. 선택한 사진들이 해당 TravelPlan에 속하는지 확인
        if (request.getSelectedPhotoIds() != null && !request.getSelectedPhotoIds().isEmpty()) {
            validatePhotosBelongToTravelPlan(request.getSelectedPhotoIds(), request.getTravelPlanId(), author.getId());
        }

        // 3. TravelPost 생성
        TravelPost travelPost = TravelPost.builder()
                .travelPlan(travelPlan)
                .author(author)
                .title(request.getTitle())
                .content(request.getContent())
                .coverImageUrl(request.getCoverImageUrl())
                .visibility(request.getVisibility() != null ? request.getVisibility() : PostVisibility.PUBLIC)
                .expenseDisplayType(request.getExpenseDisplayType() != null ? request.getExpenseDisplayType() : ExpenseDisplayType.TOTAL_ONLY)
                .memoDisplayType(request.getMemoDisplayType() != null ? request.getMemoDisplayType() : MemoDisplayType.MY_MEMO_ONLY)
                .build();

        TravelPost savedPost = travelPostRepository.save(travelPost);

        // 4. 선택한 사진 연결
        if (request.getSelectedPhotoIds() != null && !request.getSelectedPhotoIds().isEmpty()) {
            int orderIndex = 0;
            for (Long photoId : request.getSelectedPhotoIds()) {
                Photo photo = photoRepository.findById(photoId)
                        .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));
                
                TravelPostPhoto travelPostPhoto = TravelPostPhoto.builder()
                        .travelPost(savedPost)
                        .photo(photo)
                        .orderIndex(orderIndex++)
                        .build();
                travelPostPhotoRepository.save(travelPostPhoto);
            }
        }

        return travelPostMapper.toDto(savedPost, author.getId());
    }

    /**
     * 여행기 목록 조회 (PUBLIC만)
     */
    public Page<TravelPostDto> getTravelPosts(int page, int size, String sort, Long authorId) {
        Pageable pageable = createPageable(page, size, sort);
        
        Page<TravelPost> postPage;
        if (authorId != null) {
            postPage = travelPostRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
        } else {
            postPage = travelPostRepository.findByVisibilityOrderByCreatedAtDesc(PostVisibility.PUBLIC, pageable);
        }

        return postPage.map(post -> travelPostMapper.toDto(post, null));
    }

    /**
     * 여행기 상세 조회
     */
    public TravelPostDto getTravelPostById(Long postId, Long currentUserId) {
        TravelPost travelPost = travelPostRepository.findByIdWithAuthorAndPlan(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 접근 권한 확인
        validateAccess(travelPost, currentUserId);

        // 조회수 증가 (작성자가 아닌 경우)
        if (currentUserId == null || !travelPost.getAuthor().getId().equals(currentUserId)) {
            incrementViewCount(travelPost);
        }

        // TravelPlan 조회 (필터링된 데이터 포함)
        TravelPlan travelPlan = travelPlanRepository.findByIdWithDays(travelPost.getTravelPlan().getId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPost.getTravelPlan().getId()));

        // ExpenseDisplayType과 MemoDisplayType에 따라 데이터 필터링
        TravelPlan filteredTravelPlan = filterTravelPlanData(travelPlan, travelPost.getExpenseDisplayType(), 
                travelPost.getMemoDisplayType(), travelPost.getAuthor().getId());

        // 댓글 로드
        TravelPost postWithComments = loadComments(travelPost);

        return travelPostMapper.toDetailDto(postWithComments, currentUserId, filteredTravelPlan);
    }

    /**
     * shareCode로 여행기 조회 (인증 불필요)
     */
    public TravelPostDto getTravelPostByShareCode(String shareCode) {
        TravelPost travelPost = travelPostRepository.findByShareCode(shareCode)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "shareCode", shareCode));

        // LINK_ONLY 또는 PUBLIC만 조회 가능
        if (travelPost.getVisibility() == PostVisibility.PRIVATE) {
            throw new ForbiddenException("비공개 여행기는 링크로 접근할 수 없습니다.");
        }

        // 조회수 증가
        incrementViewCount(travelPost);

        // TravelPlan 조회 및 필터링
        TravelPlan travelPlan = travelPlanRepository.findByIdWithDays(travelPost.getTravelPlan().getId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPost.getTravelPlan().getId()));

        TravelPlan filteredTravelPlan = filterTravelPlanData(travelPlan, travelPost.getExpenseDisplayType(),
                travelPost.getMemoDisplayType(), travelPost.getAuthor().getId());

        TravelPost postWithComments = loadComments(travelPost);

        return travelPostMapper.toDetailDto(postWithComments, null, filteredTravelPlan);
    }

    /**
     * 여행기 수정
     */
    @Transactional
    public TravelPostDto updateTravelPost(Long postId, UpdateTravelPostRequest request, User currentUser) {
        TravelPost travelPost = travelPostRepository.findByIdWithAuthorAndPlan(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 작성자만 수정 가능
        if (!travelPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("여행기를 수정할 권한이 없습니다.");
        }

        // 정보 업데이트
        if (request.getTitle() != null) {
            travelPost.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            travelPost.setContent(request.getContent());
        }
        if (request.getCoverImageUrl() != null) {
            travelPost.setCoverImageUrl(request.getCoverImageUrl());
        }
        if (request.getVisibility() != null) {
            travelPost.setVisibility(request.getVisibility());
            // visibility가 변경되면 shareCode 재생성 필요할 수 있음
            if ((request.getVisibility() == PostVisibility.LINK_ONLY || request.getVisibility() == PostVisibility.PUBLIC) 
                    && travelPost.getShareCode() == null) {
                travelPost.setShareCode(generateShareCode());
            }
        }
        if (request.getExpenseDisplayType() != null) {
            travelPost.setExpenseDisplayType(request.getExpenseDisplayType());
        }
        if (request.getMemoDisplayType() != null) {
            travelPost.setMemoDisplayType(request.getMemoDisplayType());
        }

        // 사진 업데이트
        if (request.getSelectedPhotoIds() != null) {
            // 기존 사진 제거
            travelPostPhotoRepository.deleteByTravelPostId(postId);
            
            // 새 사진 연결
            if (!request.getSelectedPhotoIds().isEmpty()) {
                validatePhotosBelongToTravelPlan(request.getSelectedPhotoIds(), travelPost.getTravelPlan().getId(), currentUser.getId());
                int orderIndex = 0;
                for (Long photoId : request.getSelectedPhotoIds()) {
                    Photo photo = photoRepository.findById(photoId)
                            .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));
                    
                    TravelPostPhoto travelPostPhoto = TravelPostPhoto.builder()
                            .travelPost(travelPost)
                            .photo(photo)
                            .orderIndex(orderIndex++)
                            .build();
                    travelPostPhotoRepository.save(travelPostPhoto);
                }
            }
        }

        TravelPost updatedPost = travelPostRepository.save(travelPost);
        return travelPostMapper.toDto(updatedPost, currentUser.getId());
    }

    /**
     * 여행기 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteTravelPost(Long postId, User currentUser) {
        TravelPost travelPost = travelPostRepository.findByIdWithAuthorAndPlan(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 작성자만 삭제 가능
        if (!travelPost.getAuthor().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("여행기를 삭제할 권한이 없습니다.");
        }

        travelPostRepository.delete(travelPost);
    }

    /**
     * 좋아요 추가/취소 (토글)
     */
    @Transactional
    public LikeResponse toggleLike(Long postId, User currentUser) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 접근 권한 확인
        validateAccess(travelPost, currentUser.getId());

        TravelPostLike existingLike = travelPostLikeRepository
                .findByTravelPostIdAndUserId(postId, currentUser.getId())
                .orElse(null);

        boolean isLiked;
        if (existingLike != null) {
            // 좋아요 취소
            travelPostLikeRepository.delete(existingLike);
            travelPost.decrementLikeCount();
            isLiked = false;
        } else {
            // 좋아요 추가
            TravelPostLike like = TravelPostLike.builder()
                    .travelPost(travelPost)
                    .user(currentUser)
                    .build();
            travelPostLikeRepository.save(like);
            travelPost.incrementLikeCount();
            isLiked = true;
        }

        travelPostRepository.save(travelPost);

        return LikeResponse.builder()
                .isLiked(isLiked)
                .likeCount(travelPost.getLikeCount())
                .build();
    }

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentDto createComment(Long postId, CreateCommentRequest request, User currentUser) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        // 접근 권한 및 댓글 작성 권한 확인
        validateCommentAccess(travelPost, currentUser.getId());

        TravelPostComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = travelPostCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", request.getParentCommentId()));
            if (!parentComment.getTravelPost().getId().equals(postId)) {
                throw new BadRequestException("부모 댓글이 해당 여행기에 속하지 않습니다.");
            }
        }

        TravelPostComment comment = TravelPostComment.builder()
                .travelPost(travelPost)
                .user(currentUser)
                .parentComment(parentComment)
                .content(request.getContent())
                .build();

        TravelPostComment savedComment = travelPostCommentRepository.save(comment);
        travelPost.incrementCommentCount();
        travelPostRepository.save(travelPost);

        return travelPostMapper.toCommentDto(savedComment, currentUser.getId());
    }

    /**
     * 댓글 목록 조회
     */
    public Page<CommentDto> getComments(Long postId, int page, int size) {
        TravelPost travelPost = travelPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", postId));

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPostComment> commentPage = travelPostCommentRepository
                .findParentCommentsByTravelPostId(postId, pageable);

        return commentPage.map(comment -> {
            // 대댓글 로드
            List<TravelPostComment> replies = travelPostCommentRepository
                    .findRepliesByParentCommentId(comment.getId());
            comment.setReplies(replies);
            return travelPostMapper.toCommentDto(comment, null);
        });
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public CommentDto updateComment(Long postId, Long commentId, UpdateCommentRequest request, User currentUser) {
        TravelPostComment comment = travelPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", commentId));

        if (!comment.getTravelPost().getId().equals(postId)) {
            throw new BadRequestException("댓글이 해당 여행기에 속하지 않습니다.");
        }

        // 작성자만 수정 가능
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("댓글을 수정할 권한이 없습니다.");
        }

        comment.setContent(request.getContent());
        TravelPostComment updatedComment = travelPostCommentRepository.save(comment);

        return travelPostMapper.toCommentDto(updatedComment, currentUser.getId());
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, User currentUser) {
        TravelPostComment comment = travelPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", commentId));

        if (!comment.getTravelPost().getId().equals(postId)) {
            throw new BadRequestException("댓글이 해당 여행기에 속하지 않습니다.");
        }

        // 작성자만 삭제 가능
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("댓글을 삭제할 권한이 없습니다.");
        }

        TravelPost travelPost = comment.getTravelPost();
        travelPostCommentRepository.delete(comment);
        travelPost.decrementCommentCount();
        travelPostRepository.save(travelPost);
    }

    /**
     * 댓글 좋아요 추가/취소 (토글)
     */
    @Transactional
    public LikeResponse toggleCommentLike(Long postId, Long commentId, User currentUser) {
        TravelPostComment comment = travelPostCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글", "id", commentId));

        if (!comment.getTravelPost().getId().equals(postId)) {
            throw new BadRequestException("댓글이 해당 여행기에 속하지 않습니다.");
        }

        TravelPostCommentLike existingLike = travelPostCommentLikeRepository
                .findByCommentIdAndUserId(commentId, currentUser.getId())
                .orElse(null);

        boolean isLiked;
        if (existingLike != null) {
            // 좋아요 취소
            travelPostCommentLikeRepository.delete(existingLike);
            comment.decrementLikeCount();
            isLiked = false;
        } else {
            // 좋아요 추가
            TravelPostCommentLike like = TravelPostCommentLike.builder()
                    .comment(comment)
                    .user(currentUser)
                    .build();
            travelPostCommentLikeRepository.save(like);
            comment.incrementLikeCount();
            isLiked = true;
        }

        travelPostCommentRepository.save(comment);

        return LikeResponse.builder()
                .isLiked(isLiked)
                .likeCount(comment.getLikeCount())
                .build();
    }

    /**
     * 내 여행기 목록 조회
     */
    public Page<TravelPostDto> getMyTravelPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPost> postPage = travelPostRepository.findByAuthorId(userId, pageable);

        return postPage.map(post -> travelPostMapper.toDto(post, userId));
    }

    /**
     * 좋아요한 여행기 목록 조회
     */
    public Page<TravelPostDto> getLikedTravelPosts(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPost> postPage = travelPostRepository.findLikedPostsByUserId(userId, pageable);

        return postPage.map(post -> {
            TravelPostDto dto = travelPostMapper.toDto(post, userId);
            dto.setIsLiked(true); // 좋아요한 목록이므로 항상 true
            return dto;
        });
    }

    // ==================== Private Helper Methods ====================

    /**
     * 사진들이 해당 TravelPlan에 속하는지 확인
     */
    private void validatePhotosBelongToTravelPlan(List<Long> photoIds, Long travelPlanId, Long userId) {
        for (Long photoId : photoIds) {
            Photo photo = photoRepository.findByIdWithTravelPlan(photoId)
                    .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));
            
            if (!photo.getTravelPlan().getId().equals(travelPlanId)) {
                throw new BadRequestException("사진이 해당 여행 계획에 속하지 않습니다. photoId: " + photoId);
            }
            
            // 권한 확인: SHARED 또는 본인 사진만
            if (photo.getVisibility() != PhotoVisibility.SHARED && !photo.getUser().getId().equals(userId)) {
                throw new ForbiddenException("본인의 공개 사진만 선택할 수 있습니다. photoId: " + photoId);
            }
        }
    }

    /**
     * 접근 권한 확인
     * - PRIVATE: 작성자만 조회 가능 (인증 필수)
     * - LINK_ONLY: 누구나 조회 가능 (게스트 포함)
     * - PUBLIC: 누구나 조회 가능 (게스트 포함)
     */
    private void validateAccess(TravelPost travelPost, Long currentUserId) {
        PostVisibility visibility = travelPost.getVisibility();
        
        // PRIVATE인 경우에만 작성자 확인
        if (visibility == PostVisibility.PRIVATE) {
            if (currentUserId == null || !travelPost.getAuthor().getId().equals(currentUserId)) {
                throw new ForbiddenException("비공개 여행기는 작성자만 조회할 수 있습니다.");
            }
        }
        // LINK_ONLY, PUBLIC은 누구나 조회 가능 (게스트 모드 포함)
    }

    /**
     * 댓글 작성 권한 확인
     * - 댓글 작성은 인증된 사용자만 가능
     * - PRIVATE 여행기는 작성자만 댓글 작성 가능
     */
    private void validateCommentAccess(TravelPost travelPost, Long currentUserId) {
        // 댓글 작성은 인증 필수
        if (currentUserId == null) {
            throw new ForbiddenException("댓글을 작성하려면 로그인이 필요합니다.");
        }
        
        // PRIVATE 여행기는 작성자만 댓글 작성 가능
        if (travelPost.getVisibility() == PostVisibility.PRIVATE) {
            if (!travelPost.getAuthor().getId().equals(currentUserId)) {
                throw new ForbiddenException("비공개 여행기는 작성자만 댓글을 작성할 수 있습니다.");
            }
        }
    }

    /**
     * 조회수 증가 (트랜잭션 분리)
     */
    @Transactional
    public void incrementViewCount(TravelPost travelPost) {
        travelPost.incrementViewCount();
        travelPostRepository.save(travelPost);
    }

    /**
     * TravelPlan 데이터 필터링 (ExpenseDisplayType, MemoDisplayType에 따라)
     */
    private TravelPlan filterTravelPlanData(TravelPlan travelPlan, ExpenseDisplayType expenseDisplayType, 
                                           MemoDisplayType memoDisplayType, Long authorId) {
        // TravelPlan 복사 (엔티티는 직접 수정하지 않고, DTO 변환 시 필터링)
        // 실제로는 DTO 변환 시 필터링하므로, 여기서는 TravelPlan을 그대로 반환
        // 필터링은 Mapper나 별도 메서드에서 처리
        return travelPlan;
    }

    /**
     * 댓글 로드 (대댓글 포함)
     */
    private TravelPost loadComments(TravelPost travelPost) {
        // 댓글과 대댓글을 로드
        List<TravelPostComment> parentComments = travelPostCommentRepository
                .findParentCommentsByTravelPostId(travelPost.getId(), Pageable.unpaged())
                .getContent();
        
        for (TravelPostComment parentComment : parentComments) {
            List<TravelPostComment> replies = travelPostCommentRepository
                    .findRepliesByParentCommentId(parentComment.getId());
            parentComment.setReplies(replies);
        }
        
        travelPost.setComments(parentComments);
        return travelPost;
    }

    /**
     * Pageable 생성
     */
    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
        
        if (sort != null) {
            switch (sort.toLowerCase()) {
                case "likes":
                    sortObj = Sort.by(Sort.Direction.DESC, "likeCount");
                    break;
                case "views":
                    sortObj = Sort.by(Sort.Direction.DESC, "viewCount");
                    break;
                case "created":
                default:
                    sortObj = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }
        
        return PageRequest.of(page, size, sortObj);
    }

    /**
     * shareCode 생성
     */
    private String generateShareCode() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}

