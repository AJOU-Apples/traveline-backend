package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.PhotoDto;
import org.apples.travelinebackend.entity.Photo;
import org.springframework.stereotype.Component;

@Component
public class PhotoMapper {
    
    public PhotoDto toDto(Photo photo) {
        if (photo == null) {
            return null;
        }
        
        return PhotoDto.builder()
                .id(photo.getId())
                .travelPlanId(photo.getTravelPlan() != null ? photo.getTravelPlan().getId() : null)
                .travelDayId(photo.getTravelDay() != null ? photo.getTravelDay().getId() : null)
                .dayNumber(photo.getTravelDay() != null ? photo.getTravelDay().getDayNumber() : null)
                .placeId(photo.getPlace() != null ? photo.getPlace().getId() : null)
                .userId(photo.getUser() != null ? photo.getUser().getId() : null)
                .username(photo.getUser() != null ? photo.getUser().getNickname() : null)
                .uri(photo.getUri())
                .thumbnailUri(photo.getThumbnailUri())
                .filename(photo.getFilename())
                .fileSize(photo.getFileSize())
                .mimeType(photo.getMimeType())
                .width(photo.getWidth())
                .height(photo.getHeight())
                .latitude(photo.getLatitude())
                .longitude(photo.getLongitude())
                .timestamp(photo.getTimestamp())
                .uploadedAt(photo.getUploadedAt())
                .visibility(photo.getVisibility())
                .caption(photo.getCaption())
                .orderIndex(photo.getOrderIndex())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .build();
    }
}

