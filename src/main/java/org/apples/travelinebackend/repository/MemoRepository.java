package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {

    @Query("SELECT m FROM Memo m WHERE m.place.id = :placeId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<Memo> findByPlaceIdAndDeletedAtIsNull(@Param("placeId") Long placeId);

    @Query("SELECT m FROM Memo m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Memo> findByIdAndDeletedAtIsNull(@Param("id") Long id);
}

