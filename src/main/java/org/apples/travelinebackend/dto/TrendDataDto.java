package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataDto {
    private List<TrendPoint> mau;
    private List<TrendPoint> travelPlans;
    private List<TrendPoint> travelPosts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private Long value;
    }
}

