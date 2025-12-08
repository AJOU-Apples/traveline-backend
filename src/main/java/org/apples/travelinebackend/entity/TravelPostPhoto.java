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

@Entity
@Table(name = "travel_post_photos", indexes = {
        @Index(name = "idx_travel_post_photo_post", columnList = "travel_post_id"),
        @Index(name = "idx_travel_post_photo_order", columnList = "travel_post_id, order_index")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPostPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPost travelPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photo_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Photo photo;

    @Column(nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

