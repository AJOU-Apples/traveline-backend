package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "travel_plans", indexes = {
        @Index(name = "idx_travelplan_user", columnList = "user_id"),
        @Index(name = "idx_travelplan_status", columnList = "status"),
        @Index(name = "idx_travelplan_dates", columnList = "start_date, end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE travel_plans SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class TravelPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    private City destination;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Integer participants;

    // 작성자 (추가) - 레거시 호환성을 위해 유지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 여행 계획 멤버 (다대다 관계)
    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TravelPlanMember> members = new ArrayList<>();

    // 여행 상태 (추가)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TravelPlanStatus status = TravelPlanStatus.PLANNING;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isArchived = false;

    @OneToMany(mappedBy = "travelPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @Builder.Default
    private List<TravelDay> days = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    public void addDay(TravelDay day) {
        days.add(day);
        day.setTravelPlan(this);
    }

    public void removeDay(TravelDay day) {
        days.remove(day);
        day.setTravelPlan(null);
    }

    /**
     * 여행 기간 계산 (일수)
     */
    public Integer getDuration() {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    // ==================== 멤버 관리 헬퍼 메서드 ====================

    /**
     * 여행 계획 소유자 조회
     */
    public User getOwner() {
        return members.stream()
                .filter(m -> m.getRole() == MemberRole.OWNER && m.getStatus() == InvitationStatus.ACCEPTED)
                .findFirst()
                .map(TravelPlanMember::getUser)
                .orElse(user); // fallback to legacy user field
    }

    /**
     * 수락된 멤버 수 조회
     */
    public Integer getAcceptedMembersCount() {
        return (int) members.stream()
                .filter(m -> m.getStatus() == InvitationStatus.ACCEPTED)
                .count();
    }

    /**
     * 특정 사용자가 이 여행 계획에 접근 권한이 있는지 확인
     */
    public boolean hasAccess(Long userId) {
        if (userId == null)
            return false;

        // 레거시 체크
        if (user != null && user.getId().equals(userId)) {
            return true;
        }

        return members.stream()
                .anyMatch(m -> m.getUser().getId().equals(userId)
                        && m.getStatus() == InvitationStatus.ACCEPTED);
    }

    /**
     * 특정 사용자가 특정 역할 이상의 권한을 가지고 있는지 확인
     */
    public boolean hasRole(Long userId, MemberRole requiredRole) {
        if (userId == null || requiredRole == null)
            return false;

        // 레거시 체크 - 기존 user는 OWNER로 취급
        if (user != null && user.getId().equals(userId)) {
            return MemberRole.OWNER.hasPermission(requiredRole);
        }

        Optional<TravelPlanMember> memberOpt = members.stream()
                .filter(m -> m.getUser().getId().equals(userId)
                        && m.getStatus() == InvitationStatus.ACCEPTED)
                .findFirst();

        if (memberOpt.isEmpty()) {
            return false;
        }

        return memberOpt.get().getRole().hasPermission(requiredRole);
    }

    /**
     * 특정 사용자가 소유자인지 확인
     */
    public boolean isOwner(Long userId) {
        return hasRole(userId, MemberRole.OWNER) &&
                members.stream()
                        .anyMatch(m -> m.getUser().getId().equals(userId)
                                && m.getRole() == MemberRole.OWNER
                                && m.getStatus() == InvitationStatus.ACCEPTED);
    }

    /**
     * 특정 사용자의 역할 조회
     */
    public Optional<MemberRole> getMemberRole(Long userId) {
        if (userId == null)
            return Optional.empty();

        // 레거시 체크
        if (user != null && user.getId().equals(userId)) {
            return Optional.of(MemberRole.OWNER);
        }

        return members.stream()
                .filter(m -> m.getUser().getId().equals(userId)
                        && m.getStatus() == InvitationStatus.ACCEPTED)
                .findFirst()
                .map(TravelPlanMember::getRole);
    }

    /**
     * 멤버 추가 헬퍼
     */
    public void addMember(TravelPlanMember member) {
        members.add(member);
        member.setTravelPlan(this);
    }

    /**
     * 멤버 제거 헬퍼
     */
    public void removeMember(TravelPlanMember member) {
        members.remove(member);
        member.setTravelPlan(null);
    }
}
