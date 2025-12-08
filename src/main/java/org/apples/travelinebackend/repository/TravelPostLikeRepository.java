package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPostLikeRepository extends JpaRepository<TravelPostLike, Long> {
    Optional<TravelPostLike> findByTravelPostIdAndUserId(Long travelPostId, Long userId);
    boolean existsByTravelPostIdAndUserId(Long travelPostId, Long userId);
    long countByTravelPostId(Long travelPostId);
}

