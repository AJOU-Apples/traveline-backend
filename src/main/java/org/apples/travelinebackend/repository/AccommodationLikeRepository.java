package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.AccommodationLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccommodationLikeRepository extends JpaRepository<AccommodationLike, Long> {
    Optional<AccommodationLike> findByAccommodationIdAndUserId(Long accommodationId, Long userId);
    boolean existsByAccommodationIdAndUserId(Long accommodationId, Long userId);
    long countByAccommodationId(Long accommodationId);
    
    @Query("SELECT al.user.id FROM AccommodationLike al WHERE al.accommodation.id = :accommodationId")
    List<Long> findUserIdsByAccommodationId(@Param("accommodationId") Long accommodationId);
}

