package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.TravelPlanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
    
    /**
     * ID로 TravelPlan 조회 (members 포함)
     * 권한 체크를 위해 members를 함께 로드
     */
    @Query("SELECT tp FROM TravelPlan tp " +
           "LEFT JOIN FETCH tp.members " +
           "WHERE tp.id = :id")
    Optional<TravelPlan> findByIdWithMembers(@Param("id") Long id);
    
    @Query("SELECT DISTINCT tp FROM TravelPlan tp LEFT JOIN FETCH tp.days ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllWithDays();
    
    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
           "LEFT JOIN FETCH tp.days " +
           "LEFT JOIN FETCH tp.members " +
           "WHERE tp.id = :id")
    Optional<TravelPlan> findByIdWithDays(@Param("id") Long id);
    
    // 전체 조회 (페이징 지원)
    @Query("SELECT tp FROM TravelPlan tp ORDER BY tp.startDate DESC")
    Page<TravelPlan> findAllWithPaging(Pageable pageable);
    
    // 아카이브되지 않은 여행 계획 조회 (페이징 지원)
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.isArchived = false ORDER BY tp.startDate DESC")
    Page<TravelPlan> findByIsArchivedFalse(Pageable pageable);
    
    // User 기반 조회 메서드들
    
    /**
     * 특정 유저의 여행 계획 조회 (일차 정보 포함)
     * 소유자이거나 멤버인 여행 모두 조회
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
           "LEFT JOIN FETCH tp.days " +
           "LEFT JOIN tp.members m " +
           "WHERE tp.user.id = :userId OR (m.user.id = :userId AND m.status = 'ACCEPTED') " +
           "ORDER BY tp.startDate DESC")
    List<TravelPlan> findByUserIdWithDays(@Param("userId") Long userId);
    
    /**
     * 특정 유저의 여행 계획 조회 (상태 필터링)
     * 소유자이거나 멤버인 여행 모두 조회
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
           "LEFT JOIN tp.members m " +
           "WHERE (tp.user.id = :userId OR (m.user.id = :userId AND m.status = 'ACCEPTED')) " +
           "AND (:status IS NULL OR tp.status = :status) " +
           "AND (:isArchived IS NULL OR tp.isArchived = :isArchived) " +
           "ORDER BY tp.startDate DESC")
    List<TravelPlan> findByUserIdAndFilters(@Param("userId") Long userId,
                                            @Param("status") TravelPlanStatus status,
                                            @Param("isArchived") Boolean isArchived);
    
    /**
     * 특정 유저의 다가오는 여행 조회 (시작일 기준 가장 가까운 여행)
     * 소유자이거나 멤버인 여행 모두 조회
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
           "LEFT JOIN tp.members m " +
           "WHERE (tp.user.id = :userId OR (m.user.id = :userId AND m.status = 'ACCEPTED')) " +
           "AND tp.startDate >= :today " +
           "AND tp.status IN ('PLANNING', 'ONGOING') " +
           "AND tp.isArchived = false " +
           "ORDER BY tp.startDate ASC")
    List<TravelPlan> findUpcomingTravelsByUserId(@Param("userId") Long userId,
                                                  @Param("today") LocalDate today);
    
    /**
     * 특정 유저의 여행 계획 조회 (ID와 User로 검증)
     * 소유자이거나 멤버인 경우 조회 가능
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp " +
           "LEFT JOIN FETCH tp.days " +
           "LEFT JOIN tp.members m " +
           "WHERE tp.id = :id " +
           "AND (tp.user.id = :userId OR (m.user.id = :userId AND m.status = 'ACCEPTED'))")
    Optional<TravelPlan> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 특정 기간 동안 생성된 여행 계획 수
     */
    @Query("SELECT COUNT(tp) FROM TravelPlan tp WHERE tp.createdAt >= :startDate AND tp.createdAt < :endDate")
    long countByCreatedAtBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                 @Param("endDate") java.time.LocalDateTime endDate);
}

