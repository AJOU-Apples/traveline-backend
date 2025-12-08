package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TravelPostMapper {

    private final TravelPlanMapper travelPlanMapper;
    private final PhotoMapper photoMapper;

    /**
     * TravelPost Entity를 DTO로 변환 (기본)
     */
    public TravelPostDto toDto(TravelPost entity) {
        if (entity == null) {
            return null;
        }

        return TravelPostDto.builder()
                .id(entity.getId())
                .travelPlanId(entity.getTravelPlan().getId())
                .author(toUserDto(entity.getAuthor()))
                .title(entity.getTitle())
                .content(entity.getContent())
                .coverImageUrl(entity.getCoverImageUrl())
                .visibility(entity.getVisibility())
                .shareCode(entity.getShareCode())
                .expenseDisplayType(entity.getExpenseDisplayType())
                .memoDisplayType(entity.getMemoDisplayType())
                .viewCount(entity.getViewCount())
                .likeCount(entity.getLikeCount())
                .commentCount(entity.getCommentCount())
                .photos(entity.getPhotos().stream()
                        .map(tpp -> photoMapper.toDto(tpp.getPhoto()))
                        .collect(Collectors.toList()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * TravelPost Entity를 DTO로 변환 (사용자 좋아요 여부 포함)
     */
    public TravelPostDto toDto(TravelPost entity, Long currentUserId) {
        TravelPostDto dto = toDto(entity);
        if (dto != null && currentUserId != null) {
            boolean isLiked = entity.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUserId));
            dto.setIsLiked(isLiked);
        }
        return dto;
    }

    /**
     * TravelPost Entity를 상세 DTO로 변환 (TravelPlan, Comments 포함)
     */
    public TravelPostDto toDetailDto(TravelPost entity, Long currentUserId, TravelPlan travelPlan) {
        TravelPostDto dto = toDto(entity, currentUserId);
        if (dto != null) {
            // TravelPlan 정보 추가
            if (travelPlan != null) {
                dto.setTravelPlan(travelPlanMapper.toDto(travelPlan));
            }

            // 댓글 정보 추가
            List<CommentDto> parentComments = entity.getComments().stream()
                    .filter(comment -> comment.getParentComment() == null)
                    .map(comment -> toCommentDto(comment, currentUserId))
                    .collect(Collectors.toList());
            dto.setComments(parentComments);
        }
        return dto;
    }

    /**
     * TravelPostComment Entity를 DTO로 변환
     */
    public CommentDto toCommentDto(TravelPostComment entity, Long currentUserId) {
        if (entity == null) {
            return null;
        }

        CommentDto dto = CommentDto.builder()
                .id(entity.getId())
                .travelPostId(entity.getTravelPost().getId())
                .user(toUserDto(entity.getUser()))
                .parentCommentId(entity.getParentComment() != null ? entity.getParentComment().getId() : null)
                .content(entity.getContent())
                .likeCount(entity.getLikeCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        // 사용자 좋아요 여부
        if (currentUserId != null) {
            boolean isLiked = entity.getLikes().stream()
                    .anyMatch(like -> like.getUser().getId().equals(currentUserId));
            dto.setIsLiked(isLiked);
        }

        // 대댓글 추가
        if (entity.getReplies() != null && !entity.getReplies().isEmpty()) {
            List<CommentDto> replies = entity.getReplies().stream()
                    .map(reply -> toCommentDto(reply, currentUserId))
                    .collect(Collectors.toList());
            dto.setReplies(replies);
        }

        return dto;
    }

    /**
     * User Entity를 UserDto로 변환
     */
    private UserDto toUserDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .username(entity.getNickname())
                .profileImage(entity.getProfileImage())
                .bio(entity.getBio())
                .isActive(entity.getIsActive())
                .isVerified(entity.getIsVerified())
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}

