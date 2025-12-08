package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel_plan_supplies", indexes = {
        @Index(name = "idx_supply_travelplan", columnList = "travel_plan_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;

    @Column(nullable = false, length = 200)
    private String text;  // 준비물 이름

    @Column
    private Integer quantity;  // 수량

    @Column(length = 20)
    private String unit;  // 단위 (개, 세트 등)

    @Column(length = 50)
    private String category;  // 분류 (전자제품, 서류 등)

    @Column(columnDefinition = "TEXT")
    private String memo;  // 비고

    @Column(nullable = false)
    @Builder.Default
    private Boolean checked = false;  // 체크 여부

    @Column
    private LocalDateTime checkedAt;  // 체크 시간

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;  // Soft delete
}

