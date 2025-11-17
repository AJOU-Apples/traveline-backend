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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "places", indexes = {
        @Index(name = "idx_place_travelday", columnList = "travel_day_id"),
        @Index(name = "idx_place_order", columnList = "travel_day_id, order_index")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id", nullable = false)
    private TravelDay travelDay;

    // 장소 정보
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String address;

    // 위치 정보
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 200)
    private String placeId;  // Google Place ID

    // 방문 정보
    @Column(length = 10)
    private String time;  // "14:00"

    @Column(nullable = false)
    private Integer orderIndex;  // 순서 (0부터 시작)

    // 메모
    @Column(columnDefinition = "TEXT")
    private String memo;  // 공유 메모

    // Personal Memos는 JSON으로 저장
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, String> personalMemos = new HashMap<>();  // {userId: memo}

    // 방문 상태
    @Column(nullable = false)
    @Builder.Default
    private Boolean isVisited = false;

    @Column
    private LocalDateTime visitedAt;

    // 타임스탬프
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long createdBy;  // User ID
}


