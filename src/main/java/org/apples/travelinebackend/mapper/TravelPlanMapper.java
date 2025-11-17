package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TravelPlanMapper {
    
    private final TravelPlanMemberMapper memberMapper;
    
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
                .participants(entity.getAcceptedMembersCount())  // 실제 수락된 멤버 수
                .isArchived(entity.getIsArchived())
                .days(entity.getDays().stream()
                        .map(this::toDayDto)
                        .collect(Collectors.toList()))
                .members(entity.getMembers().stream()
                        .filter(m -> m.getStatus() == InvitationStatus.ACCEPTED)
                        .map(memberMapper::toDto)
                        .collect(Collectors.toList()))
                .build();
    }
    
    /**
     * 현재 사용자 ID를 포함한 DTO 변환 (역할 정보 포함)
     */
    public TravelPlanDto toDtoWithRole(TravelPlan entity, Long currentUserId) {
        TravelPlanDto dto = toDto(entity);
        if (dto != null && currentUserId != null) {
            dto.setMyRole(entity.getMemberRole(currentUserId).orElse(null));
        }
        return dto;
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

