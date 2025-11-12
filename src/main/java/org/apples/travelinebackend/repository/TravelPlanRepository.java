package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPlan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

