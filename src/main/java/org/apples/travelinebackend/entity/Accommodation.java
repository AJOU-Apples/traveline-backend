package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "accommodations", indexes = {
        @Index(name = "idx_accommodation_travelplan", columnList = "travel_plan_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    // 숙소 정보
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 200)
    private String placeId;  // Google Place ID

    // 체크인/체크아웃 정보
    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column
    private LocalTime checkInTime;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column
    private LocalTime checkOutTime;

    // 예약 정보
    @Column(length = 100)
    private String confirmationNumber;  // 예약 번호

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isConfirmed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSelected = false;  // 최종 선택 여부

    // 연락처
    @Column(length = 50)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    // 웹사이트/예약 링크
    @Column(length = 500)
    private String bookingUrl;

    // 메모
    @Column(columnDefinition = "TEXT")
    private String memo;

    // 생성자 정보
    @Column(nullable = false)
    private Long createdBy;  // User ID

    // 타임스탬프
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;  // Soft delete
}

