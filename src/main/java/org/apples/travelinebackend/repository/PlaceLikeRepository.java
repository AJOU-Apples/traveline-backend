package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.PlaceLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceLikeRepository extends JpaRepository<PlaceLike, Long> {
    Optional<PlaceLike> findByPlaceIdAndUserId(Long placeId, Long userId);
    boolean existsByPlaceIdAndUserId(Long placeId, Long userId);
    long countByPlaceId(Long placeId);
    
    @Query("SELECT pl.user.id FROM PlaceLike pl WHERE pl.place.id = :placeId")
    List<Long> findUserIdsByPlaceId(@Param("placeId") Long placeId);
}

