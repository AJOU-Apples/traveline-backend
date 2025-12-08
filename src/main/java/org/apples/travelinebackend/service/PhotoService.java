package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.PhotoDto;
import org.apples.travelinebackend.dto.UpdatePhotoRequest;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.PhotoMapper;
import org.apples.travelinebackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelDayRepository travelDayRepository;
    private final PlaceRepository placeRepository;
    private final FileStorageService fileStorageService;
    private final ImageMetadataService imageMetadataService;
    private final PhotoMapper photoMapper;
    private final WebSocketEventService webSocketEventService;

    /**
     * 사진 업로드
     */
    @Transactional
    public PhotoDto uploadPhoto(MultipartFile file,
                                 Long travelPlanId,
                                 Integer dayNumber,
                                 Long placeId,
                                 PhotoVisibility visibility,
                                 String caption,
                                 User user) throws IOException {
        // 파일 검증
        if (file.isEmpty()) {
            throw new BadRequestException("업로드할 파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("이미지 파일만 업로드 가능합니다.");
        }

        // TravelPlan 조회 및 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("사진을 업로드할 권한이 없습니다.");
        }

        // TravelDay 조회 (optional)
        TravelDay travelDay = null;
        if (dayNumber != null) {
            travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(travelPlanId, dayNumber)
                    .orElse(null);
        }

        // Place 조회 (optional)
        Place place = null;
        if (placeId != null) {
            place = placeRepository.findById(placeId)
                    .orElse(null);
        }

        // 파일 저장
        String fileUri = fileStorageService.uploadFile(file, "photos");
        String thumbnailUri = fileStorageService.uploadThumbnail(file, "thumbnails", 300, 300);

        // 이미지 메타데이터 추출
        ImageMetadataService.ImageMetadata metadata = imageMetadataService.extractMetadata(file);

        // Photo 엔티티 생성
        Photo photo = Photo.builder()
                .travelPlan(travelPlan)
                .travelDay(travelDay)
                .place(place)
                .user(user)
                .uri(fileUri)
                .thumbnailUri(thumbnailUri)
                .filename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .mimeType(contentType)
                .width(metadata.getWidth())
                .height(metadata.getHeight())
                .latitude(metadata.getLatitude())
                .longitude(metadata.getLongitude())
                .timestamp(metadata.getTimestamp())
                .uploadedAt(LocalDateTime.now())
                .visibility(visibility != null ? visibility : PhotoVisibility.SHARED)
                .caption(caption)
                .build();

        // orderIndex 자동 설정 (장소가 있는 경우, visibility별로)
        if (place != null) {
            PhotoVisibility finalVisibility = visibility != null ? visibility : PhotoVisibility.SHARED;
            Integer maxOrderIndex = photoRepository.findMaxOrderIndexByPlaceIdAndVisibility(
                place.getId(), 
                finalVisibility
            );
            photo.setOrderIndex(maxOrderIndex + 1);
        }

        Photo savedPhoto = photoRepository.save(photo);
        log.info("사진 업로드 완료: photoId={}, userId={}, filename={}", 
                savedPhoto.getId(), user.getId(), savedPhoto.getFilename());

        PhotoDto photoDto = photoMapper.toDto(savedPhoto);
        
        // WebSocket 이벤트 브로드캐스트
        webSocketEventService.broadcastPhotoAdded(travelPlanId, photoDto);

        return photoDto;
    }

    /**
     * 특정 장소의 사진 목록 조회
     */
    public List<PhotoDto> getPhotosByPlace(Long placeId, Long userId) {
        // Place 존재 확인
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));

        // 권한 검증
        if (!place.getTravelDay().getTravelPlan().hasRole(userId, org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }

        List<Photo> photos = photoRepository.findByPlaceIdWithVisibility(placeId, userId);
        return photos.stream()
                .map(photoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 사진 목록 조회
     */
    public List<PhotoDto> getPhotosByDay(Long travelPlanId, Integer dayNumber, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.hasRole(userId, org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // TravelDay 조회
        TravelDay travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(travelPlanId, dayNumber)
                .orElseThrow(() -> new ResourceNotFoundException("여행 일차", "dayNumber", dayNumber));

        List<Photo> photos = photoRepository.findByTravelDayIdWithVisibility(travelDay.getId(), userId);
        return photos.stream()
                .map(photoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 여행 계획의 모든 사진 조회
     */
    public List<PhotoDto> getPhotosByTravelPlan(Long travelPlanId, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.hasRole(userId, org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        List<Photo> photos = photoRepository.findByTravelPlanIdWithVisibility(travelPlanId, userId);
        return photos.stream()
                .map(photoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사진 상세 조회
     */
    public PhotoDto getPhotoById(Long photoId, Long userId) {
        Photo photo = photoRepository.findByIdWithTravelPlan(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));

        // 권한 검증 (여행 계획 멤버 또는 사진 업로더)
        boolean hasPlanAccess = photo.getTravelPlan().hasAccess(userId);
        boolean isUploader = photo.getUser().getId().equals(userId);
        boolean isShared = photo.getVisibility() == PhotoVisibility.SHARED;

        if (!hasPlanAccess && !isUploader && !isShared) {
            throw new ForbiddenException("사진을 볼 권한이 없습니다.");
        }

        return photoMapper.toDto(photo);
    }

    /**
     * 사진 수정 (캡션, 공개 설정, 장소 연결)
     */
    @Transactional
    public PhotoDto updatePhoto(Long photoId, UpdatePhotoRequest request, Long userId) {
        Photo photo = photoRepository.findByIdWithTravelPlan(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));

        // 권한 검증 (사진 업로더만 수정 가능)
        if (!photo.getUser().getId().equals(userId)) {
            throw new ForbiddenException("사진을 수정할 권한이 없습니다.");
        }

        // 캡션 수정
        if (request.getCaption() != null) {
            photo.setCaption(request.getCaption());
        }

        // 공개 설정 수정
        if (request.getVisibility() != null) {
            photo.setVisibility(request.getVisibility());
        }

        // 장소 연결 수정
        if (request.getPlaceId() != null) {
            Place place = placeRepository.findById(request.getPlaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("장소", "id", request.getPlaceId()));

            // Place가 같은 TravelPlan에 속하는지 확인
            if (!place.getTravelDay().getTravelPlan().getId().equals(photo.getTravelPlan().getId())) {
                throw new BadRequestException("다른 여행 계획의 장소에는 연결할 수 없습니다.");
            }

            photo.setPlace(place);
            photo.setTravelDay(place.getTravelDay());
        }

        Photo updatedPhoto = photoRepository.save(photo);
        log.info("사진 수정 완료: photoId={}, userId={}", photoId, userId);

        return photoMapper.toDto(updatedPhoto);
    }

    /**
     * 사진 삭제
     */
    @Transactional
    public void deletePhoto(Long photoId, Long userId) throws IOException {
        Photo photo = photoRepository.findByIdWithTravelPlan(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("사진", "id", photoId));

        // 권한 검증 (사진 업로더 또는 여행 계획 EDITOR 이상만 삭제 가능)
        boolean hasPermission = photo.getTravelPlan().hasRole(userId, org.apples.travelinebackend.entity.MemberRole.EDITOR);
        boolean isUploader = photo.getUser().getId().equals(userId);

        if (!hasPermission && !isUploader) {
            throw new ForbiddenException("사진을 삭제할 권한이 없습니다.");
        }

        // 파일 삭제
        try {
            fileStorageService.deleteFile(photo.getUri());
            fileStorageService.deleteFile(photo.getThumbnailUri());
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            // 파일 삭제 실패해도 DB에서는 삭제 진행
        }

        photoRepository.delete(photo);
        log.info("사진 삭제 완료: photoId={}, userId={}", photoId, userId);
    }

    /**
     * 사진 순서 변경 (visibility별로 독립적으로 관리)
     */
    @Transactional
    public List<PhotoDto> reorderPhotos(Long placeId, PhotoVisibility visibility, List<Long> photoIds, Long userId) {
        if (placeId == null) {
            throw new BadRequestException("장소 ID는 필수입니다.");
        }
        
        if (visibility == null) {
            throw new BadRequestException("공개 설정은 필수입니다.");
        }

        // Place 조회 및 권한 검증
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));

        if (!place.getTravelDay().getTravelPlan().hasRole(userId, org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }

        // 해당 visibility의 기존 사진 목록 조회
        List<Photo> existingPhotos = photoRepository.findByPlaceIdAndVisibilityOrderByOrderIndex(placeId, visibility);

        // 요청된 photoIds와 기존 사진들이 일치하는지 검증
        if (existingPhotos.size() != photoIds.size()) {
            throw new BadRequestException("사진 개수가 일치하지 않습니다. 기존: " + existingPhotos.size() + ", 요청: " + photoIds.size());
        }

        // PhotoId를 Key로 하는 Map 생성
        Map<Long, Photo> photoMap = existingPhotos.stream()
                .collect(Collectors.toMap(Photo::getId, photo -> photo));

        // 새로운 순서대로 orderIndex 업데이트
        for (int i = 0; i < photoIds.size(); i++) {
            Long photoId = photoIds.get(i);
            Photo photo = photoMap.get(photoId);

            if (photo == null) {
                throw new ResourceNotFoundException("사진", "id", photoId);
            }

            // 다른 장소의 사진인지 확인
            if (!photo.getPlace().getId().equals(placeId)) {
                throw new BadRequestException("다른 장소의 사진은 순서를 변경할 수 없습니다.");
            }
            
            // 다른 visibility의 사진인지 확인
            if (photo.getVisibility() != visibility) {
                throw new BadRequestException("다른 공개 설정의 사진은 함께 순서를 변경할 수 없습니다.");
            }

            photo.setOrderIndex(i);
        }

        photoRepository.saveAll(existingPhotos);
        log.info("사진 순서 변경 완료: placeId={}, visibility={}, userId={}", placeId, visibility, userId);

        // 변경된 사진 목록 반환 (해당 visibility만)
        List<Photo> reorderedPhotos = photoRepository.findByPlaceIdAndVisibilityOrderByOrderIndex(placeId, visibility);
        return reorderedPhotos.stream()
                .map(photoMapper::toDto)
                .collect(Collectors.toList());
    }
}

