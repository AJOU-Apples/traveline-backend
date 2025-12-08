package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Memo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemoRepository extends JpaRepository<Memo, Long> {

    @Query("SELECT m FROM Memo m WHERE m.place.id = :placeId AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    List<Memo> findByPlaceIdAndDeletedAtIsNull(@Param("placeId") Long placeId);
    
    /**
     * 특정 장소의 메모 조회 (권한 필터링 - SHARED 또는 본인 메모만)
     */
    @Query("SELECT m FROM Memo m " +
           "JOIN FETCH m.author " +
           "WHERE m.place.id = :placeId " +
           "AND m.deletedAt IS NULL " +
           "AND (m.visibility = 'SHARED' OR m.author.id = :userId) " +
           "ORDER BY m.createdAt DESC")
    List<Memo> findByPlaceIdWithVisibility(@Param("placeId") Long placeId,
                                            @Param("userId") Long userId);

    @Query("SELECT m FROM Memo m WHERE m.id = :id AND m.deletedAt IS NULL")
    Optional<Memo> findByIdAndDeletedAtIsNull(@Param("id") Long id);
    
    /**
     * 특정 장소의 모든 메모를 강제로 삭제 (하드 삭제)
     */
    @Modifying
    @Query("DELETE FROM Memo m WHERE m.place.id = :placeId")
    int hardDeleteByPlaceId(@Param("placeId") Long placeId);
}

