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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "travel_posts", indexes = {
        @Index(name = "idx_travel_post_plan", columnList = "travel_plan_id"),
        @Index(name = "idx_travel_post_author", columnList = "author_id"),
        @Index(name = "idx_travel_post_visibility", columnList = "visibility"),
        @Index(name = "idx_travel_post_share_code", columnList = "share_code", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE travel_posts SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL AND is_hidden = false")
public class TravelPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User author;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String coverImageUrl;

    // 공유 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PostVisibility visibility = PostVisibility.PUBLIC;

    @Column(length = 50, unique = true)
    private String shareCode; // 링크 공유용 고유 코드

    // 표기 범위 설정
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExpenseDisplayType expenseDisplayType = ExpenseDisplayType.TOTAL_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MemoDisplayType memoDisplayType = MemoDisplayType.MY_MEMO_ONLY;

    // 통계
    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    // 숨김 처리 (관리자 신고 처리용)
    // 주의: 기존 데이터 마이그레이션 후 nullable = false로 변경 필요
    @Column(name = "is_hidden")
    @Builder.Default
    private Boolean isHidden = false;

    // Relations
    @OneToMany(mappedBy = "travelPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TravelPostPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "travelPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TravelPostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "travelPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TravelPostComment> comments = new ArrayList<>();

    // 메타데이터
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    /**
     * shareCode 자동 생성 (LINK_ONLY 또는 PUBLIC인 경우)
     */
    @PrePersist
    public void generateShareCode() {
        if (shareCode == null && (visibility == PostVisibility.LINK_ONLY || visibility == PostVisibility.PUBLIC)) {
            shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        }
    }

    /**
     * 조회수 증가
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * 좋아요 수 증가
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 댓글 수 증가
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * 댓글 수 감소
     */
    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
