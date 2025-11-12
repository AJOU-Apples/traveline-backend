package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
public class TravelPlanMapper {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    public TravelPlanDto toDto(TravelPlan entity) {
        if (entity == null) {
            return null;
        }
        
        return TravelPlanDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .destination(toCityDto(entity.getDestination()))
                .startDate(entity.getStartDate().format(DATE_FORMATTER))
                .endDate(entity.getEndDate().format(DATE_FORMATTER))
                .participants(entity.getParticipants())
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
                .date(entity.getDate().format(DATE_FORMATTER))
                .displayDate(entity.getDisplayDate())
                .build();
    }
    
    public CityDto toCityDto(City entity) {
        if (entity == null) {
            return null;
        }
        
        return CityDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .isInternational(entity.getIsInternational())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .currency(entity.getCurrency())
                .build();
    }
}

