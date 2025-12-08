package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelPostPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelPostPhotoRepository extends JpaRepository<TravelPostPhoto, Long> {
    List<TravelPostPhoto> findByTravelPostIdOrderByOrderIndexAsc(Long travelPostId);
    void deleteByTravelPostId(Long travelPostId);
}

