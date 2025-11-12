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
    
    @Query("SELECT DISTINCT tp FROM TravelPlan tp LEFT JOIN FETCH tp.days ORDER BY tp.createdAt DESC")
    List<TravelPlan> findAllWithDays();
    
    @Query("SELECT DISTINCT tp FROM TravelPlan tp LEFT JOIN FETCH tp.days WHERE tp.id = :id")
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
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp LEFT JOIN FETCH tp.days WHERE tp.user.id = :userId ORDER BY tp.startDate DESC")
    List<TravelPlan> findByUserIdWithDays(@Param("userId") Long userId);
    
    /**
     * 특정 유저의 여행 계획 조회 (상태 필터링)
     */
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId " +
           "AND (:status IS NULL OR tp.status = :status) " +
           "AND (:isArchived IS NULL OR tp.isArchived = :isArchived) " +
           "ORDER BY tp.startDate DESC")
    List<TravelPlan> findByUserIdAndFilters(@Param("userId") Long userId,
                                            @Param("status") TravelPlanStatus status,
                                            @Param("isArchived") Boolean isArchived);
    
    /**
     * 특정 유저의 다가오는 여행 조회 (시작일 기준 가장 가까운 여행)
     */
    @Query("SELECT tp FROM TravelPlan tp WHERE tp.user.id = :userId " +
           "AND tp.startDate >= :today " +
           "AND tp.status IN ('PLANNING', 'ONGOING') " +
           "AND tp.isArchived = false " +
           "ORDER BY tp.startDate ASC")
    List<TravelPlan> findUpcomingTravelsByUserId(@Param("userId") Long userId,
                                                  @Param("today") LocalDate today);
    
    /**
     * 특정 유저의 여행 계획 조회 (ID와 User로 검증)
     */
    @Query("SELECT DISTINCT tp FROM TravelPlan tp LEFT JOIN FETCH tp.days WHERE tp.id = :id AND tp.user.id = :userId")
    Optional<TravelPlan> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

