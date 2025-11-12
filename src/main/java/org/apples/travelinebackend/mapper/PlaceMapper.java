package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.entity.Place;
import org.springframework.stereotype.Component;

@Component
public class PlaceMapper {
    
    public PlaceDto toDto(Place place) {
        if (place == null) {
            return null;
        }
        
        return PlaceDto.builder()
                .id(place.getId())
                .travelDayId(place.getTravelDay() != null ? place.getTravelDay().getId() : null)
                .travelPlanId(place.getTravelDay() != null && place.getTravelDay().getTravelPlan() != null 
                        ? place.getTravelDay().getTravelPlan().getId() : null)
                .dayNumber(place.getTravelDay() != null ? place.getTravelDay().getDayNumber() : null)
                .name(place.getName())
                .address(place.getAddress())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .placeId(place.getPlaceId())
                .time(place.getTime())
                .orderIndex(place.getOrderIndex())
                .memo(place.getMemo())
                .personalMemos(place.getPersonalMemos())
                .isVisited(place.getIsVisited())
                .visitedAt(place.getVisitedAt())
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .createdBy(place.getCreatedBy())
                .build();
    }
}


