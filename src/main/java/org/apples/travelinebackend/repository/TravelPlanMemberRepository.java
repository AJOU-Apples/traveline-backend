package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.InvitationStatus;
import org.apples.travelinebackend.entity.MemberRole;
import org.apples.travelinebackend.entity.TravelPlanMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelPlanMemberRepository extends JpaRepository<TravelPlanMember, Long> {

    /**
     * 특정 여행 계획의 모든 멤버 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "JOIN FETCH m.user " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "ORDER BY m.role ASC, m.joinedAt ASC")
    List<TravelPlanMember> findByTravelPlanIdWithUser(@Param("travelPlanId") Long travelPlanId);

    /**
     * 특정 여행 계획의 수락된 멤버만 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "JOIN FETCH m.user " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "AND m.status = 'ACCEPTED' " +
           "ORDER BY m.role ASC, m.joinedAt ASC")
    List<TravelPlanMember> findAcceptedMembersByTravelPlanId(@Param("travelPlanId") Long travelPlanId);

    /**
     * 특정 사용자의 여행 계획 멤버십 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "AND m.user.id = :userId")
    Optional<TravelPlanMember> findByTravelPlanIdAndUserId(
            @Param("travelPlanId") Long travelPlanId,
            @Param("userId") Long userId);

    /**
     * 사용자가 특정 역할 이상의 권한을 가지고 있는지 확인
     */
    @Query("SELECT COUNT(m) > 0 FROM TravelPlanMember m " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "AND m.user.id = :userId " +
           "AND m.status = 'ACCEPTED' " +
           "AND m.role = :role")
    boolean hasRole(@Param("travelPlanId") Long travelPlanId,
                    @Param("userId") Long userId,
                    @Param("role") MemberRole role);

    /**
     * 특정 사용자의 모든 초대 목록 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "JOIN FETCH m.travelPlan tp " +
           "WHERE m.user.id = :userId " +
           "AND m.status = :status " +
           "ORDER BY m.invitedAt DESC")
    List<TravelPlanMember> findByUserIdAndStatus(@Param("userId") Long userId,
                                                   @Param("status") InvitationStatus status);

    /**
     * 특정 사용자가 참여중인 여행 계획의 멤버십 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "JOIN FETCH m.travelPlan tp " +
           "WHERE m.user.id = :userId " +
           "AND m.status = 'ACCEPTED' " +
           "AND tp.deletedAt IS NULL " +
           "ORDER BY m.joinedAt DESC")
    List<TravelPlanMember> findAcceptedMembershipsByUserId(@Param("userId") Long userId);

    /**
     * 여행 계획의 소유자 조회
     */
    @Query("SELECT m FROM TravelPlanMember m " +
           "JOIN FETCH m.user " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "AND m.role = 'OWNER' " +
           "AND m.status = 'ACCEPTED'")
    Optional<TravelPlanMember> findOwnerByTravelPlanId(@Param("travelPlanId") Long travelPlanId);

    /**
     * 특정 여행 계획의 멤버 수 조회 (수락된 멤버만)
     */
    @Query("SELECT COUNT(m) FROM TravelPlanMember m " +
           "WHERE m.travelPlan.id = :travelPlanId " +
           "AND m.status = 'ACCEPTED'")
    Integer countAcceptedMembersByTravelPlanId(@Param("travelPlanId") Long travelPlanId);

    /**
     * 여행 계획과 사용자 ID로 멤버 삭제
     */
    void deleteByTravelPlanIdAndUserId(Long travelPlanId, Long userId);
}

