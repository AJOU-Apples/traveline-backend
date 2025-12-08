package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("SELECT f FROM Flight f WHERE f.travelPlan.id = :travelPlanId AND f.deletedAt IS NULL ORDER BY f.departureTime")
    List<Flight> findByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT f FROM Flight f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Flight> findByIdAndDeletedAtIsNull(@Param("id") Long id);
}

