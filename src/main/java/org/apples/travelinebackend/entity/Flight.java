package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights", indexes = {
        @Index(name = "idx_flight_travelplan", columnList = "travel_plan_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    private TravelPlan travelPlan;

    // 항공편 정보
    @Column(nullable = false, length = 100)
    private String airline; // 항공사

    @Column(nullable = false, length = 20)
    private String flightNumber; // 항공편명 (예: KE123)

    // 출발 정보
    @Column(nullable = false, length = 100)
    private String departureAirport; // 출발 공항

    @Column(length = 10)
    private String departureAirportCode; // IATA 코드 (예: ICN)

    @Column(nullable = false)
    private LocalDateTime departureTime;

    // 도착 정보
    @Column(nullable = false, length = 100)
    private String arrivalAirport; // 도착 공항

    @Column(length = 10)
    private String arrivalAirportCode; // IATA 코드 (예: NRT)

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    // 예약 정보
    @Column(length = 100)
    private String confirmationNumber; // 예약 번호 (PNR)

    @Column(length = 20)
    private String seatNumber; // 좌석 번호

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isConfirmed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isSelected = false; // 최종 선택 여부

    // 추가 정보
    @Column(length = 50)
    private String cabinClass; // 이코노미, 비즈니스 등

    @Column(length = 100)
    private String passengerName; // 탑승자 이름

    @Column(length = 500)
    private String bookingUrl; // 예약 링크

    // 메모
    @Column(columnDefinition = "TEXT")
    private String memo;

    // 생성자 정보
    @Column(nullable = false)
    private Long createdBy; // User ID

    // 타임스탬프
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt; // Soft delete
}
