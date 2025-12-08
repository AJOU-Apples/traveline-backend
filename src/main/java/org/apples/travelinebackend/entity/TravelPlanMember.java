package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 여행 계획 멤버 (다대다 관계 중간 테이블)
 */
@Entity
@Table(name = "travel_plan_members", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_travelplan_user", columnNames = {"travel_plan_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_member_travelplan", columnList = "travel_plan_id"),
        @Index(name = "idx_member_user", columnList = "user_id"),
        @Index(name = "idx_member_status", columnList = "status")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlanMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * 멤버의 역할
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    /**
     * 초대 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    /**
     * 초대된 시간
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime invitedAt;

    /**
     * 초대를 수락한 시간
     */
    @Column
    private LocalDateTime joinedAt;

    /**
     * 초대한 사람 (선택)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User invitedBy;
}

