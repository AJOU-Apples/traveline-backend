package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.TravelPlanStatus;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPlanService {

    private final TravelPlanRepository travelPlanRepository;
    private final CityRepository cityRepository;
    private final TravelPlanMapper travelPlanMapper;

    @Transactional
    public TravelPlanDto createTravelPlan(CreateTravelPlanRequest request, User user) {
        LocalDate startDate = LocalDate.parse(request.getStartDate());
        LocalDate endDate = LocalDate.parse(request.getEndDate());

        // destination City 조회
        City destination = null;
        if (request.getDestinationId() != null) {
            destination = cityRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException("도시", "id", request.getDestinationId()));
        }

        TravelPlan travelPlan = TravelPlan.builder()
                .title(request.getTitle())
                .destination(destination)
                .startDate(startDate)
                .endDate(endDate)
                .participants(request.getParticipants())
                .user(user)  // User 정보 추가
                .build();

        // 일차별 데이터 추가
        if (request.getDays() != null && !request.getDays().isEmpty()) {
            request.getDays().forEach(dayDto -> {
                TravelDay travelDay = TravelDay.builder()
                        .dayNumber(dayDto.getDayNumber())
                        .date(LocalDate.parse(dayDto.getDate()))
                        .displayDate(dayDto.getDisplayDate())
                        .build();

                travelPlan.addDay(travelDay);
            });
        } else {
            // request.getDays()가 null이거나 비어있으면 자동 생성
            List<TravelDay> generatedDays = generateTravelDays(startDate, endDate);
            generatedDays.forEach(travelPlan::addDay);
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

        boolean datesChanged = false;
        
        // 기본 정보 업데이트
        if (request.getTitle() != null) {
            travelPlan.setTitle(request.getTitle());
        }
        if (request.getDestinationId() != null) {
            City destination = cityRepository.findById(request.getDestinationId())
                    .orElseThrow(() -> new ResourceNotFoundException("도시", "id", request.getDestinationId()));
            travelPlan.setDestination(destination);
        }
        if (request.getStartDate() != null) {
            LocalDate startDate = LocalDate.parse(request.getStartDate());
            if (!startDate.equals(travelPlan.getStartDate())) {
                travelPlan.setStartDate(startDate);
                datesChanged = true;
            }
        }
        if (request.getEndDate() != null) {
            LocalDate endDate = LocalDate.parse(request.getEndDate());
            if (!endDate.equals(travelPlan.getEndDate())) {
                travelPlan.setEndDate(endDate);
                datesChanged = true;
            }
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
        } else if (datesChanged) {
            // 날짜가 변경되었고 days가 제공되지 않은 경우, TravelDay의 date를 업데이트
            travelPlan.getDays().clear();
            List<TravelDay> regeneratedDays = generateTravelDays(travelPlan.getStartDate(), travelPlan.getEndDate());
            regeneratedDays.forEach(travelPlan::addDay);
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

    // ==================== User 기반 메서드 ====================

    /**
     * 내 여행 계획 목록 조회 (필터링 지원)
     * 
     * @param userId     사용자 ID
     * @param status     여행 상태 (null이면 전체)
     * @param isArchived 아카이브 여부 (null이면 전체)
     * @return 필터링된 여행 계획 목록
     */
    public List<TravelPlanDto> getMyTravelPlans(Long userId, TravelPlanStatus status, Boolean isArchived) {
        List<TravelPlan> plans = travelPlanRepository.findByUserIdAndFilters(userId, status, isArchived);
        return plans.stream()
                .map(travelPlanMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 다가오는 여행 조회 (D-day용)
     * 가장 가까운 시작일을 가진 여행 계획 반환
     * 
     * @param userId 사용자 ID
     * @return 다가오는 여행 계획 (없으면 null)
     */
    public TravelPlanDto getUpcomingTravel(Long userId) {
        LocalDate today = LocalDate.now();
        List<TravelPlan> upcomingTravels = travelPlanRepository.findUpcomingTravelsByUserId(userId, today);
        
        if (upcomingTravels.isEmpty()) {
            return null;
        }
        
        return travelPlanMapper.toDto(upcomingTravels.get(0));
    }

    /**
     * 여행 계획 소유자 권한 검증
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     * @throws ResourceNotFoundException 여행 계획을 찾을 수 없는 경우
     * @throws ForbiddenException       소유자가 아닌 경우
     */
    public void validateOwnership(Long planId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", planId));
        
        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }
    }

    /**
     * 소유자 검증과 함께 여행 계획 조회
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     * @return 여행 계획 DTO
     */
    public TravelPlanDto getTravelPlanByIdWithAuth(Long planId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", planId));
        
        return travelPlanMapper.toDto(travelPlan);
    }

    /**
     * 소유자 검증과 함께 여행 계획 수정
     * 
     * @param planId  여행 계획 ID
     * @param request 수정 요청
     * @param userId  사용자 ID
     * @return 수정된 여행 계획 DTO
     */
    @Transactional
    public TravelPlanDto updateTravelPlanWithAuth(Long planId, UpdateTravelPlanRequest request, Long userId) {
        validateOwnership(planId, userId);
        return updateTravelPlan(planId, request);
    }

    /**
     * 소유자 검증과 함께 여행 계획 삭제
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteTravelPlanWithAuth(Long planId, Long userId) {
        validateOwnership(planId, userId);
        deleteTravelPlan(planId);
    }

    /**
     * 소유자 검증과 함께 여행 계획 아카이브
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     * @return 아카이브된 여행 계획 DTO
     */
    @Transactional
    public TravelPlanDto archiveTravelPlanWithAuth(Long planId, Long userId) {
        validateOwnership(planId, userId);
        return archiveTravelPlan(planId);
    }

    // ==================== Helper Methods ====================

    /**
     * startDate부터 endDate까지 TravelDay 자동 생성
     * 
     * @param startDate 시작일
     * @param endDate   종료일
     * @return 생성된 TravelDay 목록
     */
    private List<TravelDay> generateTravelDays(LocalDate startDate, LocalDate endDate) {
        List<TravelDay> travelDays = new ArrayList<>();
        int dayNumber = 1;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            TravelDay travelDay = TravelDay.builder()
                    .dayNumber(dayNumber)
                    .date(currentDate)
                    .displayDate(formatDisplayDate(currentDate))
                    .build();
            
            travelDays.add(travelDay);
            currentDate = currentDate.plusDays(1);
            dayNumber++;
        }

        return travelDays;
    }

    /**
     * 날짜를 "11월 20일(수)" 형식으로 포맷
     * 
     * @param date 날짜
     * @return 포맷된 날짜 문자열
     */
    private String formatDisplayDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일(E)", Locale.KOREAN);
        return date.format(formatter);
    }
}
