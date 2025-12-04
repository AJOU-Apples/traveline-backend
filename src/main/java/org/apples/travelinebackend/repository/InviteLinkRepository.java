package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.InviteLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InviteLinkRepository extends JpaRepository<InviteLink, Long> {

    /**
     * 코드로 초대 링크 조회
     */
    @Query("SELECT il FROM InviteLink il " +
           "JOIN FETCH il.travelPlan tp " +
           "JOIN FETCH il.createdBy " +
           "WHERE il.inviteCode = :code")
    Optional<InviteLink> findByCode(@Param("code") String code);

    /**
     * 여행 계획 ID로 활성화된 초대 링크 조회
     */
    @Query("SELECT il FROM InviteLink il " +
           "WHERE il.travelPlan.id = :travelPlanId " +
           "AND il.isActive = true " +
           "ORDER BY il.createdAt DESC")
    List<InviteLink> findByTravelPlanId(@Param("travelPlanId") Long travelPlanId);

    /**
     * 여행 계획 ID로 모든 초대 링크 삭제
     */
    void deleteByTravelPlanId(Long travelPlanId);

    /**
     * 여행 계획 ID로 활성화된 링크가 있는지 확인
     */
    @Query("SELECT COUNT(il) > 0 FROM InviteLink il " +
           "WHERE il.travelPlan.id = :travelPlanId " +
           "AND il.isActive = true")
    boolean existsActiveByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
}

