package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_days", indexes = {
        @Index(name = "idx_travelday_travelplan", columnList = "travel_plan_id"),
        @Index(name = "idx_travelday_unique", columnList = "travel_plan_id, day_number", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelDay {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer dayNumber;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private String displayDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TravelPlan travelPlan;
    
    @OneToMany(mappedBy = "travelDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Place> places = new ArrayList<>();
    
    // Helper methods
    public void addPlace(Place place) {
        places.add(place);
        place.setTravelDay(this);
    }
    
    public void removePlace(Place place) {
        places.remove(place);
        place.setTravelDay(null);
    }
}

