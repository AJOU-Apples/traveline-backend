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
@Table(name = "travel_post_likes", indexes = {
        @Index(name = "idx_travel_post_like_post", columnList = "travel_post_id"),
        @Index(name = "idx_travel_post_like_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_user", columnNames = {"travel_post_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPost travelPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

