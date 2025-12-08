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
 * 여행 계획 초대 코드 엔티티
 */
@Entity
@Table(name = "invite_links",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_invite_code", columnNames = "invite_code")
    },
    indexes = {
        @Index(name = "idx_invite_travelplan", columnList = "travel_plan_id"),
        @Index(name = "idx_invite_code", columnList = "invite_code"),
        @Index(name = "idx_invite_active", columnList = "is_active")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;

    @Column(name = "invite_code", nullable = false, unique = true, length = 6)
    private String inviteCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 코드가 유효한지 확인 (활성화되어 있고 만료되지 않았는지)
     */
    public boolean isValid() {
        return isActive && expiresAt.isAfter(LocalDateTime.now());
    }
}

