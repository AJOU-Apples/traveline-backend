package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.PostVisibility;
import org.apples.travelinebackend.entity.TravelPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPostRepository extends JpaRepository<TravelPost, Long> {

    /**
     * ID로 TravelPost 조회 (author, travelPlan 포함)
     */
    @Query("SELECT tp FROM TravelPost tp " +
           "LEFT JOIN FETCH tp.author " +
           "LEFT JOIN FETCH tp.travelPlan " +
           "WHERE tp.id = :id")
    Optional<TravelPost> findByIdWithAuthorAndPlan(@Param("id") Long id);

    /**
     * shareCode로 TravelPost 조회
     */
    Optional<TravelPost> findByShareCode(String shareCode);

    /**
     * PUBLIC 여행기 목록 조회 (페이징, 정렬)
     */
    @Query("SELECT tp FROM TravelPost tp " +
           "LEFT JOIN FETCH tp.author " +
           "WHERE tp.visibility = :visibility " +
           "ORDER BY tp.createdAt DESC")
    Page<TravelPost> findByVisibilityOrderByCreatedAtDesc(
            @Param("visibility") PostVisibility visibility,
            Pageable pageable);

    /**
     * 특정 작성자의 여행기 목록 조회
     */
    @Query("SELECT tp FROM TravelPost tp " +
           "LEFT JOIN FETCH tp.author " +
           "WHERE tp.author.id = :authorId " +
           "ORDER BY tp.createdAt DESC")
    Page<TravelPost> findByAuthorIdOrderByCreatedAtDesc(
            @Param("authorId") Long authorId,
            Pageable pageable);

    /**
     * 작성자의 모든 여행기 조회 (비공개 포함)
     */
    @Query("SELECT tp FROM TravelPost tp " +
           "LEFT JOIN FETCH tp.author " +
           "WHERE tp.author.id = :authorId " +
           "ORDER BY tp.createdAt DESC")
    Page<TravelPost> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    /**
     * TravelPlan ID로 여행기 조회
     */
    Optional<TravelPost> findByTravelPlanId(Long travelPlanId);

    /**
     * 사용자가 좋아요한 여행기 목록 조회
     */
    @Query("SELECT tp FROM TravelPost tp " +
           "LEFT JOIN FETCH tp.author " +
           "JOIN TravelPostLike tpl ON tpl.travelPost = tp " +
           "WHERE tpl.user.id = :userId " +
           "ORDER BY tpl.createdAt DESC")
    Page<TravelPost> findLikedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 특정 기간 동안 생성된 여행기 수
     */
    @Query("SELECT COUNT(tp) FROM TravelPost tp WHERE tp.createdAt >= :startDate AND tp.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                 @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * 관리자용: 숨겨진 여행기 포함하여 ID로 조회
     * @SQLRestriction을 우회하기 위해 nativeQuery 사용
     */
    @Query(value = "SELECT * FROM travel_posts WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    Optional<TravelPost> findByIdIncludingHidden(@Param("id") Long id);
}

