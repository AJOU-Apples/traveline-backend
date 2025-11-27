package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 여행 계획의 채팅 메시지를 최신순으로 페이징 조회
     */
    Page<ChatMessage> findByTravelPlanIdOrderByCreatedAtDesc(Long travelPlanId, Pageable pageable);

    /**
     * 특정 여행 계획의 채팅 메시지 수 조회
     */
    long countByTravelPlanId(Long travelPlanId);

    /**
     * 특정 여행 계획의 특정 시점 이후 메시지 조회 (최신순)
     */
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.travelPlan.id = :travelPlanId " +
           "AND cm.createdAt > :after " +
           "ORDER BY cm.createdAt ASC")
    List<ChatMessage> findByTravelPlanIdAndCreatedAtAfter(
            @Param("travelPlanId") Long travelPlanId,
            @Param("after") LocalDateTime after);
}

