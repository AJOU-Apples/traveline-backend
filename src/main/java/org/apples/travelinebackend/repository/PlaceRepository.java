package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    /**
     * 특정 TravelDay의 모든 장소 조회 (순서대로)
     */
    @Query("SELECT p FROM Place p WHERE p.travelDay.id = :travelDayId ORDER BY p.orderIndex ASC")
    List<Place> findByTravelDayIdOrderByOrderIndex(@Param("travelDayId") Long travelDayId);
    
    /**
     * 특정 TravelPlan과 dayNumber로 장소 목록 조회
     */
    @Query("SELECT p FROM Place p " +
           "JOIN p.travelDay td " +
           "WHERE td.travelPlan.id = :travelPlanId " +
           "AND td.dayNumber = :dayNumber " +
           "ORDER BY p.orderIndex ASC")
    List<Place> findByTravelPlanIdAndDayNumber(@Param("travelPlanId") Long travelPlanId,
                                                @Param("dayNumber") Integer dayNumber);
    
    /**
     * 특정 TravelDay에서 가장 높은 orderIndex 조회
     */
    @Query("SELECT COALESCE(MAX(p.orderIndex), -1) FROM Place p WHERE p.travelDay.id = :travelDayId")
    Integer findMaxOrderIndexByTravelDayId(@Param("travelDayId") Long travelDayId);
    
    /**
     * Place와 TravelDay를 함께 조회 (권한 검증용)
     */
    @Query("SELECT p FROM Place p " +
           "JOIN FETCH p.travelDay td " +
           "JOIN FETCH td.travelPlan tp " +
           "WHERE p.id = :placeId")
    Optional<Place> findByIdWithTravelPlan(@Param("placeId") Long placeId);
    
    /**
     * 특정 TravelPlan의 모든 장소 조회
     */
    @Query("SELECT p FROM Place p " +
           "JOIN p.travelDay td " +
           "WHERE td.travelPlan.id = :travelPlanId " +
           "ORDER BY td.dayNumber ASC, p.orderIndex ASC")
    List<Place> findByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
}


