package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlacesApiResponse {
    
    private List<PlaceSearchResult> results;
    private String status;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceSearchResponse {
        private List<PlaceSearchResult> results;
    }
}


