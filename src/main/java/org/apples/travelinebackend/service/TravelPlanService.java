package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.TravelPlanStatus;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPlanService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    
    private final TravelPlanRepository travelPlanRepository;
    private final TravelPlanMapper travelPlanMapper;
    
    @Transactional
    public TravelPlanDto createTravelPlan(CreateTravelPlanRequest request) {
        LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
        
        TravelPlan travelPlan = TravelPlan.builder()
                .title(request.getTitle())
                .destination(request.getDestination())
                .startDate(startDate)
                .endDate(endDate)
                .participants(request.getParticipants())
                .status(calculateStatus(startDate, endDate))
                .build();
        
        // 일차별 데이터 추가
        if (request.getDays() != null) {
            request.getDays().forEach(dayDto -> {
                TravelDay travelDay = TravelDay.builder()
                        .dayNumber(dayDto.getDayNumber())
                        .date(LocalDate.parse(dayDto.getDate()))
                        .displayDate(dayDto.getDisplayDate())
                        .build();
                
                // 장소 데이터 추가
                if (dayDto.getPlaces() != null) {
                    dayDto.getPlaces().forEach(placeDto -> {
                        Place place = Place.builder()
                                .name(placeDto.getName())
                                .address(placeDto.getAddress())
                                .time(placeDto.getTime())
                                .memo(placeDto.getMemo())
                                .latitude(placeDto.getLatitude())
                                .longitude(placeDto.getLongitude())
                                .build();
                        travelDay.addPlace(place);
                    });
                }
                
                travelPlan.addDay(travelDay);
            });
        }
        
        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        return travelPlanMapper.toDto(savedPlan);
    }
    
    public List<TravelPlanDto> getAllTravelPlans() {
        return travelPlanRepository.findAllWithDays().stream()
                .map(travelPlanMapper::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 여행 계획 목록 조회 (필터링 및 페이징 지원)
     * @param status 여행 상태 필터 (optional)
     * @param page 페이지 번호 (0부터 시작)
     * @param limit 페이지 당 개수
     * @return 페이징된 여행 계획 목록
     */
    public Page<TravelPlanDto> getTravelPlans(String status, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<TravelPlan> planPage;
        
        if (status != null && !status.isEmpty()) {
            TravelPlanStatus planStatus = TravelPlanStatus.valueOf(status.toUpperCase());
            planPage = travelPlanRepository.findByIsArchivedFalseAndStatus(planStatus, pageable);
        } else {
            planPage = travelPlanRepository.findByIsArchivedFalse(pageable);
        }
        
        return planPage.map(travelPlanMapper::toDto);
    }
    
    public TravelPlanDto getTravelPlanById(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithDays(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));
        
        // Places는 Lazy Loading으로 자동 로드됨 (트랜잭션 내에서)
        travelPlan.getDays().forEach(day -> day.getPlaces().size());
        
        return travelPlanMapper.toDto(travelPlan);
    }
    
    @Transactional
    public TravelPlanDto updateTravelPlan(Long id, UpdateTravelPlanRequest request) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));
        
        LocalDate startDate = travelPlan.getStartDate();
        LocalDate endDate = travelPlan.getEndDate();
        
        // 기본 정보 업데이트
        if (request.getTitle() != null) {
            travelPlan.setTitle(request.getTitle());
        }
        if (request.getDestination() != null) {
            travelPlan.setDestination(request.getDestination());
        }
        if (request.getStartDate() != null) {
            startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            travelPlan.setStartDate(startDate);
        }
        if (request.getEndDate() != null) {
            endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
            travelPlan.setEndDate(endDate);
        }
        if (request.getParticipants() != null) {
            travelPlan.setParticipants(request.getParticipants());
        }
        
        // 날짜가 변경된 경우 상태 재계산
        travelPlan.setStatus(calculateStatus(startDate, endDate));
        
        // 일차별 데이터 업데이트 (전체 교체)
        if (request.getDays() != null) {
            // 기존 일차 데이터 제거
            travelPlan.getDays().clear();
            
            // 새로운 일차 데이터 추가
            request.getDays().forEach(dayDto -> {
                TravelDay travelDay = TravelDay.builder()
                        .dayNumber(dayDto.getDayNumber())
                        .date(LocalDate.parse(dayDto.getDate()))
                        .displayDate(dayDto.getDisplayDate())
                        .build();
                
                // 장소 데이터 추가
                if (dayDto.getPlaces() != null) {
                    dayDto.getPlaces().forEach(placeDto -> {
                        Place place = Place.builder()
                                .name(placeDto.getName())
                                .address(placeDto.getAddress())
                                .time(placeDto.getTime())
                                .memo(placeDto.getMemo())
                                .latitude(placeDto.getLatitude())
                                .longitude(placeDto.getLongitude())
                                .build();
                        travelDay.addPlace(place);
                    });
                }
                
                travelPlan.addDay(travelDay);
            });
        }
        
        TravelPlan updatedPlan = travelPlanRepository.save(travelPlan);
        return travelPlanMapper.toDto(updatedPlan);
    }
    
    /**
     * 여행 계획 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteTravelPlan(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));
        travelPlanRepository.delete(travelPlan); // @SQLDelete에 의해 soft delete 수행
    }
    
    /**
     * 여행 계획 아카이브
     */
    @Transactional
    public TravelPlanDto archiveTravelPlan(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));
        
        travelPlan.setIsArchived(true);
        TravelPlan archivedPlan = travelPlanRepository.save(travelPlan);
        return travelPlanMapper.toDto(archivedPlan);
    }
    
    /**
     * 여행 날짜를 기반으로 상태 계산
     */
    private TravelPlanStatus calculateStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        
        if (today.isBefore(startDate)) {
            return TravelPlanStatus.UPCOMING;
        } else if (today.isAfter(endDate)) {
            return TravelPlanStatus.PAST;
        } else {
            return TravelPlanStatus.ONGOING;
        }
    }
}

