package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private MauStats mau;
    private CountStats travelPlans;
    private CountStats travelPosts;
    private UserStats users;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MauStats {
        private Long current;
        private Long previous;
        private Double change; // 변화율 (%)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountStats {
        private Long total;
        private Long thisMonth;
        private Long previousMonth;
        private Double change; // 변화율 (%)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Long total;
        private Long activeThisMonth;
        private Long newThisMonth;
    }
}

