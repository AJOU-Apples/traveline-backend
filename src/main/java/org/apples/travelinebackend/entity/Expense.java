package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses", indexes = {
        @Index(name = "idx_expense_travelplan", columnList = "travel_plan_id"),
        @Index(name = "idx_expense_place", columnList = "place_id"),
        @Index(name = "idx_expense_type", columnList = "travel_plan_id, expense_type"),
        @Index(name = "idx_expense_date", columnList = "expense_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id")
    private TravelDay travelDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false)
    private User paidBy;  // 결제한 사람

    // 지출 정보
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "KRW";

    // 지출 타입
    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false, length = 20)
    private ExpenseType type;

    // 정산 정보 (shared인 경우)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Long> splitWith = new ArrayList<>();  // [userId1, userId2] - 정산할 사람들

    @Column(precision = 10, scale = 2)
    private BigDecimal splitAmount;  // 1인당 정산 금액

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSettled = false;  // 정산 완료 여부

    // 영수증
    @Column(length = 500)
    private String receiptImage;  // S3 URL 또는 로컬 경로

    // 메모
    @Column(length = 1000)
    private String memo;

    // 날짜 및 시간
    @Column
    private LocalDate expenseDate;

    @Column(length = 10)
    private String expenseTime;  // "14:30"

    // 타임스탬프
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

