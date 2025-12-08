package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    @Query("SELECT a FROM Accommodation a WHERE a.travelPlan.id = :travelPlanId AND a.deletedAt IS NULL ORDER BY a.checkInDate")
    List<Accommodation> findByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT a FROM Accommodation a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Accommodation> findByIdAndDeletedAtIsNull(@Param("id") Long id);
}

