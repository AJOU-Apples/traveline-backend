package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.FlightLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightLikeRepository extends JpaRepository<FlightLike, Long> {
    Optional<FlightLike> findByFlightIdAndUserId(Long flightId, Long userId);
    boolean existsByFlightIdAndUserId(Long flightId, Long userId);
    long countByFlightId(Long flightId);
    
    @Query("SELECT fl.user.id FROM FlightLike fl WHERE fl.flight.id = :flightId")
    List<Long> findUserIdsByFlightId(@Param("flightId") Long flightId);
}

