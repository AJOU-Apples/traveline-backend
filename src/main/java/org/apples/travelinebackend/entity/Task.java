package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "travel_plan_tasks", indexes = {
        @Index(name = "idx_task_travelplan", columnList = "travel_plan_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    @Column(nullable = false, length = 200)
    private String text;  // 해야 할 일

    @Column
    private LocalDate deadline;  // 기한 (선택)

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

