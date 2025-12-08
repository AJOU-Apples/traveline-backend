package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TravelPostCommentLikeRepository extends JpaRepository<TravelPostCommentLike, Long> {
    Optional<TravelPostCommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
    boolean existsByCommentIdAndUserId(Long commentId, Long userId);
    long countByCommentId(Long commentId);
}

