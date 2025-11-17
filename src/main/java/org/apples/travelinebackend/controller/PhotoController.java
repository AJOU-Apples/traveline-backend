package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.PhotoDto;
import org.apples.travelinebackend.dto.ReorderPhotosRequest;
import org.apples.travelinebackend.dto.UpdatePhotoRequest;
import org.apples.travelinebackend.entity.PhotoVisibility;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.PhotoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    /**
     * 사진 업로드
     * POST /api/photos
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PhotoDto> uploadPhoto(
            @AuthenticationPrincipal User currentUser,
            @RequestParam("file") MultipartFile file,
            @RequestParam("travelPlanId") Long travelPlanId,
            @RequestParam(value = "dayNumber", required = false) Integer dayNumber,
            @RequestParam(value = "placeId", required = false) Long placeId,
            @RequestParam(value = "visibility", required = false, defaultValue = "SHARED") String visibility,
            @RequestParam(value = "caption", required = false) String caption) throws IOException {
        
        log.info("POST /api/photos - userId={}, travelPlanId={}, dayNumber={}, placeId={}", 
                currentUser.getId(), travelPlanId, dayNumber, placeId);

        PhotoVisibility photoVisibility = PhotoVisibility.valueOf(visibility.toUpperCase());

        PhotoDto uploadedPhoto = photoService.uploadPhoto(
                file, travelPlanId, dayNumber, placeId, photoVisibility, caption, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedPhoto);
    }

    /**
     * 특정 장소의 사진 목록 조회
     * GET /api/photos?placeId={placeId}
     */
    @GetMapping(params = "placeId")
    public ResponseEntity<Map<String, List<PhotoDto>>> getPhotosByPlace(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long placeId) {
        
        log.info("GET /api/photos?placeId={} - userId={}", placeId, currentUser.getId());

        List<PhotoDto> photos = photoService.getPhotosByPlace(placeId, currentUser.getId());

        Map<String, List<PhotoDto>> response = new HashMap<>();
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜의 사진 목록 조회
     * GET /api/photos?travelPlanId={planId}&dayNumber={dayNumber}
     */
    @GetMapping(params = {"travelPlanId", "dayNumber"})
    public ResponseEntity<Map<String, List<PhotoDto>>> getPhotosByDay(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId,
            @RequestParam Integer dayNumber) {
        
        log.info("GET /api/photos?travelPlanId={}&dayNumber={} - userId={}", 
                travelPlanId, dayNumber, currentUser.getId());

        List<PhotoDto> photos = photoService.getPhotosByDay(travelPlanId, dayNumber, currentUser.getId());

        Map<String, List<PhotoDto>> response = new HashMap<>();
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }

    /**
     * 여행 계획의 모든 사진 조회
     * GET /api/photos?travelPlanId={planId}
     */
    @GetMapping(params = "travelPlanId")
    public ResponseEntity<Map<String, List<PhotoDto>>> getPhotosByTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId) {
        
        log.info("GET /api/photos?travelPlanId={} - userId={}", travelPlanId, currentUser.getId());

        List<PhotoDto> photos = photoService.getPhotosByTravelPlan(travelPlanId, currentUser.getId());

        Map<String, List<PhotoDto>> response = new HashMap<>();
        response.put("photos", photos);

        return ResponseEntity.ok(response);
    }

    /**
     * 사진 상세 조회
     * GET /api/photos/{photoId}
     */
    @GetMapping("/{photoId}")
    public ResponseEntity<PhotoDto> getPhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long photoId) {
        
        log.info("GET /api/photos/{} - userId={}", photoId, currentUser.getId());

        PhotoDto photo = photoService.getPhotoById(photoId, currentUser.getId());
        return ResponseEntity.ok(photo);
    }

    /**
     * 사진 수정
     * PUT /api/photos/{photoId}
     */
    @PutMapping("/{photoId}")
    public ResponseEntity<PhotoDto> updatePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long photoId,
            @Valid @RequestBody UpdatePhotoRequest request) {
        
        log.info("PUT /api/photos/{} - userId={}", photoId, currentUser.getId());

        PhotoDto updatedPhoto = photoService.updatePhoto(photoId, request, currentUser.getId());
        return ResponseEntity.ok(updatedPhoto);
    }

    /**
     * 사진 삭제
     * DELETE /api/photos/{photoId}
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long photoId) throws IOException {
        
        log.info("DELETE /api/photos/{} - userId={}", photoId, currentUser.getId());

        photoService.deletePhoto(photoId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 사진 순서 변경 (visibility별 독립 관리)
     * PATCH /api/photos/reorder
     */
    @PatchMapping("/reorder")
    public ResponseEntity<Map<String, List<PhotoDto>>> reorderPhotos(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ReorderPhotosRequest request) {
        
        log.info("PATCH /api/photos/reorder - userId={}, placeId={}, visibility={}", 
                currentUser.getId(), request.getPlaceId(), request.getVisibility());
        
        List<PhotoDto> reorderedPhotos = photoService.reorderPhotos(
                request.getPlaceId(), 
                request.getVisibility(),
                request.getPhotoIds(), 
                currentUser.getId()
        );
        
        Map<String, List<PhotoDto>> response = new HashMap<>();
        response.put("photos", reorderedPhotos);
        
        return ResponseEntity.ok(response);
    }
}

