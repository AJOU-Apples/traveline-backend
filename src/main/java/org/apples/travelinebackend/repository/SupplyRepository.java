package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {

    @Query("SELECT s FROM Supply s WHERE s.travelPlan.id = :travelPlanId AND s.deletedAt IS NULL ORDER BY s.createdAt")
    List<Supply> findByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT s FROM Supply s WHERE s.id = :id AND s.deletedAt IS NULL")
    Optional<Supply> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM Supply s WHERE s.travelPlan.id = :travelPlanId AND s.deletedAt IS NULL")
    int countByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT COUNT(s) FROM Supply s WHERE s.travelPlan.id = :travelPlanId AND s.checked = true AND s.deletedAt IS NULL")
    int countCheckedByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
}

