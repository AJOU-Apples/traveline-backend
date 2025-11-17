package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Photo;
import org.apples.travelinebackend.entity.PhotoVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    /**
     * 특정 장소의 모든 사진의 place 참조 해제
     */
    @Modifying
    @Query("UPDATE Photo p SET p.place = NULL WHERE p.place.id = :placeId")
    int clearPlaceReference(@Param("placeId") Long placeId);
    
    /**
     * 특정 장소의 사진 조회 (권한 필터링, orderIndex 순서대로)
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.place.id = :placeId " +
           "AND (p.visibility = 'SHARED' OR p.user.id = :userId) " +
           "ORDER BY COALESCE(p.orderIndex, 999999), p.timestamp DESC")
    List<Photo> findByPlaceIdWithVisibility(@Param("placeId") Long placeId,
                                             @Param("userId") Long userId);
    
    /**
     * 특정 날짜의 사진 조회 (권한 필터링, orderIndex 순서대로)
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.travelDay.id = :travelDayId " +
           "AND (p.visibility = 'SHARED' OR p.user.id = :userId) " +
           "ORDER BY COALESCE(p.orderIndex, 999999), p.timestamp DESC")
    List<Photo> findByTravelDayIdWithVisibility(@Param("travelDayId") Long travelDayId,
                                                  @Param("userId") Long userId);
    
    /**
     * 여행 계획의 모든 사진 조회 (권한 필터링, orderIndex 순서대로)
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.travelPlan.id = :travelPlanId " +
           "AND (p.visibility = 'SHARED' OR p.user.id = :userId) " +
           "ORDER BY COALESCE(p.orderIndex, 999999), p.timestamp DESC")
    List<Photo> findByTravelPlanIdWithVisibility(@Param("travelPlanId") Long travelPlanId,
                                                   @Param("userId") Long userId);
    
    /**
     * 사진 조회 (TravelPlan과 함께 - 권한 검증용)
     */
    @Query("SELECT p FROM Photo p " +
           "JOIN FETCH p.travelPlan tp " +
           "WHERE p.id = :photoId")
    Optional<Photo> findByIdWithTravelPlan(@Param("photoId") Long photoId);
    
    /**
     * 특정 사용자가 업로드한 사진 조회
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.travelPlan.id = :travelPlanId " +
           "AND p.user.id = :userId " +
           "ORDER BY p.timestamp DESC")
    List<Photo> findByTravelPlanIdAndUserId(@Param("travelPlanId") Long travelPlanId,
                                             @Param("userId") Long userId);
    
    /**
     * 특정 장소의 사진 조회 (순서대로, 권한 무관)
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.place.id = :placeId " +
           "ORDER BY COALESCE(p.orderIndex, 999999), p.timestamp DESC")
    List<Photo> findByPlaceIdOrderByOrderIndex(@Param("placeId") Long placeId);
    
    /**
     * 특정 장소에서 가장 높은 orderIndex 조회 (visibility별)
     */
    @Query("SELECT COALESCE(MAX(p.orderIndex), -1) FROM Photo p " +
           "WHERE p.place.id = :placeId " +
           "AND p.visibility = :visibility")
    Integer findMaxOrderIndexByPlaceIdAndVisibility(@Param("placeId") Long placeId, 
                                                      @Param("visibility") PhotoVisibility visibility);
    
    /**
     * 특정 장소의 특정 visibility 사진 조회 (순서대로)
     */
    @Query("SELECT p FROM Photo p " +
           "WHERE p.place.id = :placeId " +
           "AND p.visibility = :visibility " +
           "ORDER BY COALESCE(p.orderIndex, 999999), p.timestamp DESC")
    List<Photo> findByPlaceIdAndVisibilityOrderByOrderIndex(@Param("placeId") Long placeId,
                                                              @Param("visibility") PhotoVisibility visibility);
}

