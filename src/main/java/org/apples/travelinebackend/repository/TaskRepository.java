package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.travelPlan.id = :travelPlanId AND t.deletedAt IS NULL ORDER BY t.createdAt")
    List<Task> findByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Task> findByIdAndDeletedAtIsNull(@Param("id") Long id);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.travelPlan.id = :travelPlanId AND t.deletedAt IS NULL")
    int countByTravelPlanIdAndDeletedAtIsNull(@Param("travelPlanId") Long travelPlanId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.travelPlan.id = :travelPlanId AND t.checked = true AND t.deletedAt IS NULL")
    int countCheckedByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
}

