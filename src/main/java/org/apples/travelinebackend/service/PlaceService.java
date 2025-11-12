package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.PlaceMapper;
import org.apples.travelinebackend.repository.PlaceRepository;
import org.apples.travelinebackend.repository.TravelDayRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final TravelDayRepository travelDayRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final PlaceMapper placeMapper;

    /**
     * 장소 추가
     */
    @Transactional
    public PlaceDto createPlace(CreatePlaceRequest request, Long userId) {
        // TravelPlan 조회 및 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", request.getTravelPlanId()));
        
        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }
        
        // TravelDay 조회
        TravelDay travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(
                request.getTravelPlanId(), request.getDayNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "여행 일차", "dayNumber", request.getDayNumber()));
        
        // 다음 orderIndex 계산
        Integer maxOrderIndex = placeRepository.findMaxOrderIndexByTravelDayId(travelDay.getId());
        Integer nextOrderIndex = maxOrderIndex + 1;
        
        // Place 생성
        Place place = Place.builder()
                .travelDay(travelDay)
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .placeId(request.getPlaceId())
                .time(request.getTime())
                .orderIndex(nextOrderIndex)
                .memo(request.getMemo())
                .personalMemos(new HashMap<>())
                .isVisited(false)
                .createdBy(userId)
                .build();
        
        Place savedPlace = placeRepository.save(place);
        log.info("장소 추가 완료: placeId={}, name={}, userId={}", savedPlace.getId(), savedPlace.getName(), userId);
        
        return placeMapper.toDto(savedPlace);
    }

    /**
     * 특정 날짜의 장소 목록 조회
     */
    public List<PlaceDto> getPlacesByDay(Long travelPlanId, Integer dayNumber, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));
        
        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }
        
        List<Place> places = placeRepository.findByTravelPlanIdAndDayNumber(travelPlanId, dayNumber);
        
        return places.stream()
                .map(placeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 장소 상세 조회
     */
    public PlaceDto getPlaceById(Long placeId, Long userId) {
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));
        
        // 권한 검증
        if (!place.getTravelDay().getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }
        
        return placeMapper.toDto(place);
    }

    /**
     * 장소 수정
     */
    @Transactional
    public PlaceDto updatePlace(Long placeId, UpdatePlaceRequest request, Long userId) {
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));
        
        // 권한 검증
        if (!place.getTravelDay().getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }
        
        // 필드 업데이트
        if (request.getName() != null) {
            place.setName(request.getName());
        }
        if (request.getAddress() != null) {
            place.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            place.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            place.setLongitude(request.getLongitude());
        }
        if (request.getPlaceId() != null) {
            place.setPlaceId(request.getPlaceId());
        }
        if (request.getTime() != null) {
            place.setTime(request.getTime());
        }
        if (request.getMemo() != null) {
            place.setMemo(request.getMemo());
        }
        if (request.getIsVisited() != null) {
            place.setIsVisited(request.getIsVisited());
            if (request.getIsVisited()) {
                place.setVisitedAt(LocalDateTime.now());
            } else {
                place.setVisitedAt(null);
            }
        }
        
        Place updatedPlace = placeRepository.save(place);
        log.info("장소 수정 완료: placeId={}, userId={}", placeId, userId);
        
        return placeMapper.toDto(updatedPlace);
    }

    /**
     * 장소 삭제
     */
    @Transactional
    public void deletePlace(Long placeId, Long userId) {
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));
        
        // 권한 검증
        if (!place.getTravelDay().getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }
        
        Long travelDayId = place.getTravelDay().getId();
        Integer deletedOrderIndex = place.getOrderIndex();
        
        placeRepository.delete(place);
        log.info("장소 삭제 완료: placeId={}, userId={}", placeId, userId);
        
        // 남은 장소들의 orderIndex 재조정
        reorderPlacesAfterDeletion(travelDayId, deletedOrderIndex);
    }

    /**
     * 장소 순서 변경 (Drag & Drop)
     */
    @Transactional
    public List<PlaceDto> reorderPlaces(ReorderPlacesRequest request, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", request.getTravelPlanId()));
        
        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }
        
        // TravelDay 조회
        TravelDay travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(
                request.getTravelPlanId(), request.getDayNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "여행 일차", "dayNumber", request.getDayNumber()));
        
        // 기존 장소 목록 조회
        List<Place> existingPlaces = placeRepository.findByTravelDayIdOrderByOrderIndex(travelDay.getId());
        
        // 요청된 placeIds와 기존 장소들이 일치하는지 검증
        if (existingPlaces.size() != request.getPlaceIds().size()) {
            throw new BadRequestException("장소 개수가 일치하지 않습니다.");
        }
        
        // PlaceId를 Key로 하는 Map 생성
        Map<Long, Place> placeMap = existingPlaces.stream()
                .collect(Collectors.toMap(Place::getId, place -> place));
        
        // 새로운 순서대로 orderIndex 업데이트
        for (int i = 0; i < request.getPlaceIds().size(); i++) {
            Long placeId = request.getPlaceIds().get(i);
            Place place = placeMap.get(placeId);
            
            if (place == null) {
                throw new ResourceNotFoundException("장소", "id", placeId);
            }
            
            place.setOrderIndex(i);
        }
        
        placeRepository.saveAll(existingPlaces);
        log.info("장소 순서 변경 완료: travelPlanId={}, dayNumber={}, userId={}", 
                request.getTravelPlanId(), request.getDayNumber(), userId);
        
        // 변경된 장소 목록 반환
        List<Place> reorderedPlaces = placeRepository.findByTravelDayIdOrderByOrderIndex(travelDay.getId());
        return reorderedPlaces.stream()
                .map(placeMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 장소 메모 업데이트 (공유 메모 또는 개인 메모)
     */
    @Transactional
    public PlaceDto updatePlaceMemo(Long placeId, UpdatePlaceMemoRequest request, Long userId) {
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));
        
        // 권한 검증
        if (!place.getTravelDay().getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }
        
        String type = request.getType().toLowerCase();
        
        if ("shared".equals(type)) {
            // 공유 메모 업데이트
            place.setMemo(request.getMemo());
            log.info("공유 메모 업데이트: placeId={}, userId={}", placeId, userId);
        } else if ("personal".equals(type)) {
            // 개인 메모 업데이트
            Map<String, String> personalMemos = place.getPersonalMemos();
            if (personalMemos == null) {
                personalMemos = new HashMap<>();
                place.setPersonalMemos(personalMemos);
            }
            
            if (request.getMemo() == null || request.getMemo().trim().isEmpty()) {
                // 빈 문자열이면 삭제
                personalMemos.remove(userId.toString());
            } else {
                personalMemos.put(userId.toString(), request.getMemo());
            }
            log.info("개인 메모 업데이트: placeId={}, userId={}", placeId, userId);
        } else {
            throw new BadRequestException("메모 타입은 'shared' 또는 'personal'이어야 합니다.");
        }
        
        Place updatedPlace = placeRepository.save(place);
        return placeMapper.toDto(updatedPlace);
    }

    // ==================== Private Helper Methods ====================

    /**
     * 장소 삭제 후 남은 장소들의 orderIndex 재조정
     */
    private void reorderPlacesAfterDeletion(Long travelDayId, Integer deletedOrderIndex) {
        List<Place> remainingPlaces = placeRepository.findByTravelDayIdOrderByOrderIndex(travelDayId);
        
        for (Place place : remainingPlaces) {
            if (place.getOrderIndex() > deletedOrderIndex) {
                place.setOrderIndex(place.getOrderIndex() - 1);
            }
        }
        
        placeRepository.saveAll(remainingPlaces);
        log.debug("orderIndex 재조정 완료: travelDayId={}", travelDayId);
    }
}


