package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelDayRepository extends JpaRepository<TravelDay, Long> {
    
    /**
     * TravelPlan과 dayNumber로 TravelDay 조회
     */
    @Query("SELECT td FROM TravelDay td WHERE td.travelPlan.id = :travelPlanId AND td.dayNumber = :dayNumber")
    Optional<TravelDay> findByTravelPlanIdAndDayNumber(@Param("travelPlanId") Long travelPlanId,
                                                        @Param("dayNumber") Integer dayNumber);
}

