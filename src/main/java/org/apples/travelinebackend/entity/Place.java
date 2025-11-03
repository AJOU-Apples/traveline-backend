package org.apples.travelinebackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "places")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String address;
    
    private String time;
    
    @Column(length = 1000)
    private String memo;
    
    private Double latitude;
    
    private Double longitude;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_day_id", nullable = false)
    private TravelDay travelDay;
    
    @Column(name = "display_order")
    private Integer displayOrder;
}

