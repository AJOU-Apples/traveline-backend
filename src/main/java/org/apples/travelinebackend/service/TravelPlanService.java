package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.repository.CityRepository;
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
    private final CityRepository cityRepository;
    private final TravelPlanMapper travelPlanMapper;

    @Transactional
    public TravelPlanDto createTravelPlan(CreateTravelPlanRequest request) {
        LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
        LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);

        // destination City 생성
        City destination = null;
        if (request.getDestination() != null) {
            destination = createOrUpdateCity(request.getDestination());
        }

        TravelPlan travelPlan = TravelPlan.builder()
                .title(request.getTitle())
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .participants(request.getParticipants())
                .build();

        // 일차별 데이터 추가
        if (request.getDays() != null) {
            request.getDays().forEach(dayDto -> {
                TravelDay travelDay = TravelDay.builder()
                        .dayNumber(dayDto.getDayNumber())
                        .date(LocalDate.parse(dayDto.getDate()))
                        .displayDate(dayDto.getDisplayDate())
                        .build();

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
     * 여행 계획 목록 조회 (페이징 지원)
     * 
     * @param page  페이지 번호 (0부터 시작)
     * @param limit 페이지 당 개수
     * @return 페이징된 여행 계획 목록
     */
    public Page<TravelPlanDto> getTravelPlans(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<TravelPlan> planPage = travelPlanRepository.findByIsArchivedFalse(pageable);

        return planPage.map(travelPlanMapper::toDto);
    }

    public TravelPlanDto getTravelPlanById(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithDays(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));

        return travelPlanMapper.toDto(travelPlan);
    }

    @Transactional
    public TravelPlanDto updateTravelPlan(Long id, UpdateTravelPlanRequest request) {
        TravelPlan travelPlan = travelPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));

        // 기본 정보 업데이트
        if (request.getTitle() != null) {
            travelPlan.setTitle(request.getTitle());
        }
        if (request.getDestination() != null) {
            City destination = createOrUpdateCity(request.getDestination());
            travelPlan.setDestination(destination);
        }
        if (request.getStartDate() != null) {
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            travelPlan.setStartDate(startDate);
        }
        if (request.getEndDate() != null) {
            LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
            travelPlan.setEndDate(endDate);
        }
        if (request.getParticipants() != null) {
            travelPlan.setParticipants(request.getParticipants());
        }

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
        travelPlanRepository.delete(travelPlan);
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
     * CityDto를 City 엔티티로 변환 (기존 City가 있으면 재사용, 없으면 새로 생성)
     */
    private City createOrUpdateCity(CityDto cityDto) {
        if (cityDto.getId() != null) {
            // 기존 City 조회 및 업데이트
            return cityRepository.findById(cityDto.getId())
                    .map(existingCity -> {
                        existingCity.setName(cityDto.getName());
                        existingCity.setIsInternational(cityDto.getIsInternational());
                        existingCity.setLatitude(cityDto.getLatitude());
                        existingCity.setLongitude(cityDto.getLongitude());
                        return cityRepository.save(existingCity);
                    })
                    .orElseGet(() -> createNewCity(cityDto));
        } else {
            // 새 City 생성
            return createNewCity(cityDto);
        }
    }

    private City createNewCity(CityDto cityDto) {
        City city = City.builder()
                .name(cityDto.getName())
                .isInternational(cityDto.getIsInternational())
                .latitude(cityDto.getLatitude())
                .longitude(cityDto.getLongitude())
                .build();
        return cityRepository.save(city);
    }
}
