package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPostCommentRepository extends JpaRepository<TravelPostComment, Long> {
    
    /**
     * 여행기 댓글 목록 조회 (대댓글 제외, 부모 댓글만)
     */
    @Query("SELECT c FROM TravelPostComment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.travelPost.id = :travelPostId " +
           "AND c.parentComment IS NULL " +
           "ORDER BY c.createdAt ASC")
    Page<TravelPostComment> findParentCommentsByTravelPostId(
            @Param("travelPostId") Long travelPostId,
            Pageable pageable);

    /**
     * 부모 댓글의 대댓글 목록 조회
     */
    @Query("SELECT c FROM TravelPostComment c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE c.parentComment.id = :parentCommentId " +
           "ORDER BY c.createdAt ASC")
    List<TravelPostComment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    /**
     * 여행기 댓글 수 조회
     */
    long countByTravelPostId(Long travelPostId);

    /**
     * 관리자용: 숨겨진 댓글 포함하여 ID로 조회
     * @SQLRestriction을 우회하기 위해 JPQL에서 명시적으로 조건 지정
     */
    @Query("SELECT c FROM TravelPostComment c " +
           "LEFT JOIN FETCH c.user " +
           "LEFT JOIN FETCH c.travelPost " +
           "WHERE c.id = :id AND c.deletedAt IS NULL")
    java.util.Optional<TravelPostComment> findByIdIncludingHidden(@Param("id") Long id);
}

