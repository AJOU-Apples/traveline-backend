package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.PlaceFullDto;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.repository.PlaceLikeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceMapper {

    private final PlaceLikeRepository placeLikeRepository;
    
    public PlaceDto toDto(Place place) {
        if (place == null) {
            return null;
        }
        
        // 좋아요 정보 먼저 조회
        long likeCount = placeLikeRepository.countByPlaceId(place.getId());
        
        PlaceDto dto = PlaceDto.builder()
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
                .likeCount((int) likeCount)  // Builder에서 직접 설정
                .build();
        
        return dto;
    }

    /**
     * Place를 DTO로 변환 (좋아요 정보 포함)
     */
    public PlaceDto toDto(Place place, Long userId) {
        PlaceDto dto = toDto(place);
        if (dto != null && userId != null) {
            // userId가 있을 때만 isLiked 설정 (likeCount는 이미 toDto(place)에서 설정됨)
            boolean isLiked = placeLikeRepository.existsByPlaceIdAndUserId(place.getId(), userId);
            dto.setIsLiked(isLiked);
        }
        return dto;
    }

    public PlaceFullDto toFullDto(Place place) {
        if (place == null) {
            return null;
        }
        
        // 좋아요 정보 먼저 조회
        long likeCount = placeLikeRepository.countByPlaceId(place.getId());
        
        PlaceFullDto dto = PlaceFullDto.builder()
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
                .likeCount((int) likeCount)  // Builder에서 직접 설정
                .build();
        
        return dto;
    }

    /**
     * Place를 PlaceFullDto로 변환 (좋아요 정보 포함)
     */
    public PlaceFullDto toFullDto(Place place, Long userId) {
        PlaceFullDto dto = toFullDto(place);
        if (dto != null && userId != null) {
            // userId가 있을 때만 isLiked 설정 (likeCount는 이미 toFullDto(place)에서 설정됨)
            boolean isLiked = placeLikeRepository.existsByPlaceIdAndUserId(place.getId(), userId);
            dto.setIsLiked(isLiked);
        }
        return dto;
    }
}


