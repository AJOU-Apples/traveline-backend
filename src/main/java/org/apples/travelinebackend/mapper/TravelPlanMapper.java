package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class TravelPlanMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public TravelPlanDto toDto(TravelPlan entity) {
        if (entity == null) {
            return null;
        }
        
        return TravelPlanDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .destination(entity.getDestination())
                .startDate(entity.getStartDate().format(DATE_FORMATTER))
                .endDate(entity.getEndDate().format(DATE_FORMATTER))
                .participants(entity.getParticipants())
                .status(entity.getStatus().name())
                .isArchived(entity.getIsArchived())
                .days(entity.getDays().stream()
                        .map(this::toDayDto)
                        .collect(Collectors.toList()))
                .build();
    }
    
    public TravelDayDto toDayDto(TravelDay entity) {
        if (entity == null) {
            return null;
        }
        
        return TravelDayDto.builder()
                .id(entity.getId())
                .dayNumber(entity.getDayNumber())
                .date(entity.getDate().format(ISO_DATE_FORMATTER))
                .displayDate(entity.getDisplayDate())
                .places(entity.getPlaces().stream()
                        .map(this::toPlaceDto)
                        .collect(Collectors.toList()))
                .build();
    }
    
    public PlaceDto toPlaceDto(Place entity) {
        if (entity == null) {
            return null;
        }
        
        return PlaceDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(entity.getAddress())
                .time(entity.getTime())
                .memo(entity.getMemo())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .build();
    }
}

