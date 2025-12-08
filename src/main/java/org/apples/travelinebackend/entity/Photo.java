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
@Table(name = "photos", indexes = {
        @Index(name = "idx_photo_place", columnList = "place_id"),
        @Index(name = "idx_photo_user", columnList = "user_id"),
        @Index(name = "idx_photo_travelplan", columnList = "travel_plan_id"),
        @Index(name = "idx_photo_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelDay travelDay;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Place place;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    // 파일 정보
    @Column(nullable = false, length = 500)
    private String uri;  // 파일 경로 (로컬 또는 S3 URL)

    @Column(nullable = false, length = 500)
    private String thumbnailUri;  // 썸네일 경로

    @Column(nullable = false, length = 255)
    private String filename;  // 원본 파일명

    @Column(nullable = false)
    private Long fileSize;  // bytes

    @Column(nullable = false, length = 50)
    private String mimeType;  // "image/jpeg", "image/png"

    // 이미지 정보
    @Column
    private Integer width;

    @Column
    private Integer height;

    // 위치 정보 (EXIF)
    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // 메타데이터
    @Column(nullable = false)
    private LocalDateTime timestamp;  // 촬영 시간 (EXIF에서 추출 또는 업로드 시간)

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    // 공개 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PhotoVisibility visibility = PhotoVisibility.SHARED;

    // 캡션
    @Column(length = 1000)
    private String caption;

    // 순서 (장소별, 날짜별 사진 정렬용)
    @Column
    private Integer orderIndex;

    // 타임스탬프
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

