package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.AccommodationDto;
import org.apples.travelinebackend.dto.CreateAccommodationRequest;
import org.apples.travelinebackend.dto.UpdateAccommodationRequest;
import org.apples.travelinebackend.entity.Accommodation;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.AccommodationMapper;
import org.apples.travelinebackend.repository.AccommodationRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccommodationService {

    private final AccommodationRepository accommodationRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final AccommodationMapper accommodationMapper;
    private final WebSocketEventService webSocketEventService;

    @Transactional
    public AccommodationDto createAccommodation(CreateAccommodationRequest request, User user) {
        log.info("숙소 정보 생성 요청: travelPlanId={}, userId={}", request.getTravelPlanId(), user.getId());

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + request.getTravelPlanId()));

        if (!travelPlan.hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("숙소를 등록할 권한이 없습니다");
        }

        // 체크인/아웃 날짜 검증
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new BadRequestException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다");
        }

        // 통화 자동 설정
        String currency = request.getCurrency();
        if (currency == null || currency.isEmpty()) {
            if (travelPlan.getDestination() != null && travelPlan.getDestination().getCurrency() != null) {
                currency = travelPlan.getDestination().getCurrency();
            } else {
                currency = "KRW";
            }
        }

        // Accommodation 생성
        Accommodation accommodation = Accommodation.builder()
                .travelPlan(travelPlan)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .placeId(request.getPlaceId())
                .checkInDate(request.getCheckInDate())
                .checkInTime(request.getCheckInTime())
                .checkOutDate(request.getCheckOutDate())
                .checkOutTime(request.getCheckOutTime())
                .confirmationNumber(request.getConfirmationNumber())
                .price(request.getPrice())
                .currency(currency)
                .isConfirmed(request.getIsConfirmed() != null ? request.getIsConfirmed() : false)
                .isSelected(request.getIsSelected() != null ? request.getIsSelected() : false)
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .bookingUrl(request.getBookingUrl())
                .memo(request.getMemo())
                .createdBy(user.getId())
                .build();

        Accommodation saved = accommodationRepository.save(accommodation);
        log.info("숙소 정보 생성 완료: accommodationId={}", saved.getId());

        return accommodationMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AccommodationDto> getAccommodationsByTravelPlan(Long travelPlanId, User user) {
        log.info("여행 계획별 숙소 목록 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("여행 계획에 접근할 권한이 없습니다");
        }

        List<Accommodation> accommodations = accommodationRepository.findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        return accommodations.stream()
                .map(accommodationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccommodationDto getAccommodationById(Long accommodationId, User user) {
        log.info("숙소 상세 조회: accommodationId={}", accommodationId);

        Accommodation accommodation = accommodationRepository.findByIdAndDeletedAtIsNull(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("숙소 정보를 찾을 수 없습니다: " + accommodationId));

        if (!accommodation.getTravelPlan().hasAccess(user.getId())) {
            throw new ForbiddenException("숙소 정보에 접근할 권한이 없습니다");
        }

        return accommodationMapper.toDto(accommodation);
    }

    @Transactional
    public AccommodationDto updateAccommodation(Long accommodationId, UpdateAccommodationRequest request, User user) {
        log.info("숙소 정보 수정 요청: accommodationId={}, userId={}", accommodationId, user.getId());

        Accommodation accommodation = accommodationRepository.findByIdAndDeletedAtIsNull(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("숙소 정보를 찾을 수 없습니다: " + accommodationId));

        if (!accommodation.getTravelPlan().hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("숙소 정보를 수정할 권한이 없습니다");
        }

        // 체크인/아웃 날짜 검증 (둘 다 제공된 경우만)
        if (request.getCheckInDate() != null && request.getCheckOutDate() != null) {
            if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
                throw new BadRequestException("체크아웃 날짜는 체크인 날짜보다 이후여야 합니다");
            }
        }

        // 부분 업데이트: null이 아닌 값만 업데이트
        if (request.getName() != null) accommodation.setName(request.getName());
        if (request.getAddress() != null) accommodation.setAddress(request.getAddress());
        if (request.getLatitude() != null) accommodation.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) accommodation.setLongitude(request.getLongitude());
        if (request.getPlaceId() != null) accommodation.setPlaceId(request.getPlaceId());
        if (request.getCheckInDate() != null) accommodation.setCheckInDate(request.getCheckInDate());
        if (request.getCheckInTime() != null) accommodation.setCheckInTime(request.getCheckInTime());
        if (request.getCheckOutDate() != null) accommodation.setCheckOutDate(request.getCheckOutDate());
        if (request.getCheckOutTime() != null) accommodation.setCheckOutTime(request.getCheckOutTime());
        if (request.getConfirmationNumber() != null) accommodation.setConfirmationNumber(request.getConfirmationNumber());
        if (request.getPrice() != null) accommodation.setPrice(request.getPrice());
        if (request.getCurrency() != null) accommodation.setCurrency(request.getCurrency());
        if (request.getIsConfirmed() != null) accommodation.setIsConfirmed(request.getIsConfirmed());
        if (request.getIsSelected() != null) accommodation.setIsSelected(request.getIsSelected());
        if (request.getPhoneNumber() != null) accommodation.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null) accommodation.setEmail(request.getEmail());
        if (request.getBookingUrl() != null) accommodation.setBookingUrl(request.getBookingUrl());
        if (request.getMemo() != null) accommodation.setMemo(request.getMemo());

        Accommodation updated = accommodationRepository.save(accommodation);
        log.info("숙소 정보 수정 완료: accommodationId={}", accommodationId);

        AccommodationDto accommodationDto = accommodationMapper.toDto(updated);
        
        // WebSocket 이벤트 브로드캐스트
        webSocketEventService.broadcastAccommodationUpdated(updated.getTravelPlan().getId(), accommodationDto);

        return accommodationDto;
    }

    @Transactional
    public void deleteAccommodation(Long accommodationId, User user) {
        log.info("숙소 정보 삭제 요청: accommodationId={}, userId={}", accommodationId, user.getId());

        Accommodation accommodation = accommodationRepository.findByIdAndDeletedAtIsNull(accommodationId)
                .orElseThrow(() -> new ResourceNotFoundException("숙소 정보를 찾을 수 없습니다: " + accommodationId));

        if (!accommodation.getTravelPlan().hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("숙소 정보를 삭제할 권한이 없습니다");
        }

        accommodation.setDeletedAt(LocalDateTime.now());
        accommodationRepository.save(accommodation);

        log.info("숙소 정보 삭제 완료: accommodationId={}", accommodationId);
    }
}

