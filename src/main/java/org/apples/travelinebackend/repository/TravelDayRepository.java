package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.TravelDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TravelDayRepository extends JpaRepository<TravelDay, Long> {
}

