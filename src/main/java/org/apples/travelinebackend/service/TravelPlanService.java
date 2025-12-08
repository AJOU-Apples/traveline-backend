package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.AccommodationMapper;
import org.apples.travelinebackend.mapper.ExpenseMapper;
import org.apples.travelinebackend.mapper.FlightMapper;
import org.apples.travelinebackend.mapper.MemoMapper;
import org.apples.travelinebackend.mapper.PhotoMapper;
import org.apples.travelinebackend.mapper.PlaceMapper;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.mapper.TravelPlanMemberMapper;
import org.apples.travelinebackend.repository.*;
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
    private final TravelPostRepository travelPostRepository;
    private final CityRepository cityRepository;
    private final TravelPlanMapper travelPlanMapper;
    private final TravelPlanMemberMapper travelPlanMemberMapper;
    private final PlaceRepository placeRepository;
    private final PhotoRepository photoRepository;
    private final ExpenseRepository expenseRepository;
    private final MemoRepository memoRepository;
    private final FlightRepository flightRepository;
    private final AccommodationRepository accommodationRepository;
    private final PhotoMapper photoMapper;
    private final ExpenseMapper expenseMapper;
    private final MemoMapper memoMapper;
    private final FlightMapper flightMapper;
    private final AccommodationMapper accommodationMapper;
    private final PlaceMapper placeMapper;

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
                .user(user) // User 정보 추가
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

        // 생성자를 OWNER 멤버로 자동 추가
        org.apples.travelinebackend.entity.TravelPlanMember ownerMember = org.apples.travelinebackend.entity.TravelPlanMember
                .builder()
                .travelPlan(savedPlan)
                .user(user)
                .role(org.apples.travelinebackend.entity.MemberRole.OWNER)
                .status(org.apples.travelinebackend.entity.InvitationStatus.ACCEPTED)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
        savedPlan.addMember(ownerMember);

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

    public TravelPlanDto getTravelPlanById(Long id, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithDays(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));

        return travelPlanMapper.toDtoWithRole(travelPlan, userId);
    }

    /**
     * 여행 계획 상세 조회 (권한 검증 포함)
     */
    public TravelPlanDto getTravelPlanByIdSecure(Long id, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", id));

        return travelPlanMapper.toDtoWithRole(travelPlan, userId);
    }

    @Transactional
    public TravelPlanDto updateTravelPlan(Long id, UpdateTravelPlanRequest request) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(id)
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
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + id));
        travelPlanRepository.delete(travelPlan);
    }

    /**
     * 여행 계획 아카이브
     */
    @Transactional
    public TravelPlanDto archiveTravelPlan(Long id) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(id)
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
                .map(plan -> travelPlanMapper.toDtoWithRole(plan, userId))
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

        return travelPlanMapper.toDtoWithRole(upcomingTravels.get(0), userId);
    }

    /**
     * 여행 계획 소유자 권한 검증
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     * @throws ResourceNotFoundException 여행 계획을 찾을 수 없는 경우
     * @throws ForbiddenException        소유자가 아닌 경우
     */
    public void validateOwnership(Long planId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(planId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", planId));

        if (!travelPlan.hasRole(userId, org.apples.travelinebackend.entity.MemberRole.OWNER)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다. (소유자만 가능)");
        }
    }

    /**
     * 멤버 검증과 함께 여행 계획 조회 (소유자 또는 멤버만 조회 가능)
     * 
     * @param planId 여행 계획 ID
     * @param userId 사용자 ID
     * @return 여행 계획 DTO
     */
    public TravelPlanDto getTravelPlanByIdWithAuth(Long planId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdAndUserId(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", planId));

        return travelPlanMapper.toDtoWithRole(travelPlan, userId);
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

    /**
     * 여행기에서 여행 계획 복사 (이 일정으로 계획하기)
     * 
     * @param travelPostId 복사할 여행기 ID
     * @param request      복사 요청 (제목, 날짜 등 선택적)
     * @param user         요청한 사용자 (새 여행 계획의 소유자)
     * @return 생성된 여행 계획 DTO
     */
    @Transactional
    public TravelPlanDto copyFromTravelPost(Long travelPostId, CopyTravelPlanRequest request, User user) {
        // 1. 여행기 조회
        TravelPost travelPost = travelPostRepository.findByIdWithAuthorAndPlan(travelPostId)
                .orElseThrow(() -> new ResourceNotFoundException("여행기", "id", travelPostId));

        // 2. 여행기 접근 권한 확인 (PRIVATE은 작성자만)
        if (travelPost.getVisibility() == PostVisibility.PRIVATE
                && !travelPost.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("비공개 여행기는 복사할 수 없습니다.");
        }

        // 3. 원본 여행 계획 조회
        TravelPlan sourcePlan = travelPlanRepository.findByIdWithDays(travelPost.getTravelPlan().getId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPost.getTravelPlan().getId()));

        // 4. 새 여행 계획 생성
        String newTitle = (request != null && request.getTitle() != null && !request.getTitle().isBlank())
                ? request.getTitle()
                : sourcePlan.getTitle();

        // 날짜 계산 (원본 기간 유지하면서 새 시작일 적용)
        LocalDate newStartDate;
        LocalDate newEndDate;
        long duration = java.time.temporal.ChronoUnit.DAYS.between(sourcePlan.getStartDate(), sourcePlan.getEndDate());

        if (request != null && request.getStartDate() != null && !request.getStartDate().isBlank()) {
            newStartDate = LocalDate.parse(request.getStartDate());
            if (request.getEndDate() != null && !request.getEndDate().isBlank()) {
                newEndDate = LocalDate.parse(request.getEndDate());
            } else {
                newEndDate = newStartDate.plusDays(duration);
            }
        } else {
            // 기본값: 오늘부터 시작
            newStartDate = LocalDate.now();
            newEndDate = newStartDate.plusDays(duration);
        }

        TravelPlan newPlan = TravelPlan.builder()
                .title(newTitle)
                .destination(sourcePlan.getDestination())
                .startDate(newStartDate)
                .endDate(newEndDate)
                .participants(1)
                .user(user)
                .status(TravelPlanStatus.PLANNING)
                .build();

        // 5. 일차별 데이터 복사
        List<TravelDay> sourceDays = sourcePlan.getDays().stream()
                .sorted((d1, d2) -> d1.getDayNumber().compareTo(d2.getDayNumber()))
                .collect(Collectors.toList());

        for (TravelDay sourceDay : sourceDays) {
            // 새 날짜 계산 (원본 dayNumber에 맞춰 새 시작일 기준으로)
            LocalDate newDayDate = newStartDate.plusDays(sourceDay.getDayNumber() - 1);

            TravelDay newDay = TravelDay.builder()
                    .dayNumber(sourceDay.getDayNumber())
                    .date(newDayDate)
                    .displayDate(formatDisplayDate(newDayDate))
                    .build();

            // 6. 장소 복사
            List<Place> sourcePlaces = placeRepository.findByTravelDayIdOrderByOrderIndex(sourceDay.getId());
            for (Place sourcePlace : sourcePlaces) {
                Place newPlace = Place.builder()
                        .name(sourcePlace.getName())
                        .address(sourcePlace.getAddress())
                        .latitude(sourcePlace.getLatitude())
                        .longitude(sourcePlace.getLongitude())
                        .placeId(sourcePlace.getPlaceId())
                        .time(sourcePlace.getTime())
                        .orderIndex(sourcePlace.getOrderIndex())
                        .isVisited(false) // 방문 상태는 초기화
                        .createdBy(user.getId())
                        .build();
                newDay.addPlace(newPlace);
            }

            newPlan.addDay(newDay);
        }

        TravelPlan savedPlan = travelPlanRepository.save(newPlan);

        // 7. 생성자를 OWNER 멤버로 추가
        TravelPlanMember ownerMember = TravelPlanMember.builder()
                .travelPlan(savedPlan)
                .user(user)
                .role(MemberRole.OWNER)
                .status(InvitationStatus.ACCEPTED)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
        savedPlan.addMember(ownerMember);

        return travelPlanMapper.toDto(savedPlan);
    }

    /**
     * 여행 계획 전체 데이터 조회 (여행기 생성 또는 조회용)
     * 
     * @param travelPlanId          여행 계획 ID
     * @param userId                사용자 ID (권한 체크 및 visibility 필터링용, null 가능)
     * @param travelPostId          여행기 ID (여행기 조회 컨텍스트에서 접근 시, null 가능)
     * @param includePhotos         사진 포함 여부
     * @param includeExpenses       지출 포함 여부
     * @param includeMemos          메모 포함 여부
     * @param includeFlights        항공권 포함 여부
     * @param includeAccommodations 숙소 포함 여부
     * @return 여행 계획 전체 데이터
     */
    public TravelPlanFullDto getTravelPlanFull(
            Long travelPlanId,
            Long userId,
            Long travelPostId,
            boolean includePhotos,
            boolean includeExpenses,
            boolean includeMemos,
            boolean includeFlights,
            boolean includeAccommodations) {

        // TravelPlan 조회
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 검증
        boolean hasAccess = false;

        // 1. 멤버인 경우 접근 허용
        if (userId != null && travelPlan.hasAccess(userId)) {
            hasAccess = true;
        }

        // 2. travelPostId가 제공된 경우, 해당 여행기가 PUBLIC/LINK_ONLY이고 이 여행 계획을 참조하면 접근 허용
        if (!hasAccess && travelPostId != null) {
            TravelPost travelPost = travelPostRepository.findById(travelPostId).orElse(null);
            if (travelPost != null
                    && travelPost.getTravelPlan().getId().equals(travelPlanId)
                    && travelPost.getVisibility() != PostVisibility.PRIVATE) {
                hasAccess = true;
            }
        }

        if (!hasAccess) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // 기본 TravelPlan 정보 변환
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        TravelPlanFullDto dto = TravelPlanFullDto.builder()
                .id(travelPlan.getId())
                .title(travelPlan.getTitle())
                .destination(travelPlanMapper.toCityDto(travelPlan.getDestination()))
                .startDate(travelPlan.getStartDate().format(dateFormatter))
                .endDate(travelPlan.getEndDate().format(dateFormatter))
                .participants(travelPlan.getAcceptedMembersCount())
                .isArchived(travelPlan.getIsArchived())
                .myRole(travelPlan.getMemberRole(userId).orElse(null))
                .members(travelPlan.getMembers().stream()
                        .filter(m -> m.getStatus() == InvitationStatus.ACCEPTED)
                        .map(travelPlanMemberMapper::toDto)
                        .collect(Collectors.toList()))
                .build();

        // 일차별 데이터 로드 (dayNumber 순서대로 정렬)
        List<TravelDay> days = travelPlan.getDays().stream()
                .sorted((d1, d2) -> d1.getDayNumber().compareTo(d2.getDayNumber()))
                .collect(Collectors.toList());
        for (TravelDay day : days) {
            TravelDayFullDto dayDto = TravelDayFullDto.builder()
                    .id(day.getId())
                    .dayNumber(day.getDayNumber())
                    .date(day.getDate().format(dateFormatter))
                    .displayDate(day.getDisplayDate())
                    .build();

            // 장소별 데이터 로드
            List<Place> places = placeRepository.findByTravelDayIdOrderByOrderIndex(day.getId());
            for (Place place : places) {
                PlaceFullDto placeDto = placeMapper.toFullDto(place, userId);

                // 사진 로드 (옵션)
                if (includePhotos) {
                    List<Photo> photos = photoRepository.findByPlaceIdWithVisibility(place.getId(), userId);
                    placeDto.setPhotos(photos.stream()
                            .map(photoMapper::toDto)
                            .collect(Collectors.toList()));
                }

                // 지출 로드 (옵션)
                if (includeExpenses) {
                    List<Expense> expenses = expenseRepository.findByPlaceIdWithVisibility(place.getId(), userId);
                    placeDto.setExpenses(expenses.stream()
                            .map(expenseMapper::toDto)
                            .collect(Collectors.toList()));
                }

                // 메모 로드 (옵션)
                if (includeMemos) {
                    List<Memo> memos = memoRepository.findByPlaceIdWithVisibility(place.getId(), userId);
                    placeDto.setMemos(memos.stream()
                            .map(memoMapper::toDto)
                            .collect(Collectors.toList()));
                }

                dayDto.getPlaces().add(placeDto);
            }

            dto.getDays().add(dayDto);
        }

        // 항공권 로드 (옵션)
        if (includeFlights) {
            List<Flight> flights = flightRepository.findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
            dto.setFlights(flights.stream()
                    .map(flight -> flightMapper.toDto(flight, userId))
                    .collect(Collectors.toList()));
        }

        // 숙소 로드 (옵션)
        if (includeAccommodations) {
            List<Accommodation> accommodations = accommodationRepository
                    .findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
            dto.setAccommodations(accommodations.stream()
                    .map(accommodation -> accommodationMapper.toDto(accommodation, userId))
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
