package org.apples.travelinebackend.mapper;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.AccommodationDto;
import org.apples.travelinebackend.entity.Accommodation;
import org.apples.travelinebackend.repository.AccommodationLikeRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccommodationMapper {

    private final AccommodationLikeRepository accommodationLikeRepository;

    public AccommodationDto toDto(Accommodation accommodation) {
        if (accommodation == null) {
            return null;
        }

        // 좋아요 정보 먼저 조회
        long likeCount = accommodationLikeRepository.countByAccommodationId(accommodation.getId());
        
        AccommodationDto dto = AccommodationDto.builder()
                .id(accommodation.getId())
                .travelPlanId(accommodation.getTravelPlan().getId())
                .name(accommodation.getName())
                .address(accommodation.getAddress())
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .placeId(accommodation.getPlaceId())
                .checkInDate(accommodation.getCheckInDate())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutDate(accommodation.getCheckOutDate())
                .checkOutTime(accommodation.getCheckOutTime())
                .confirmationNumber(accommodation.getConfirmationNumber())
                .price(accommodation.getPrice())
                .currency(accommodation.getCurrency())
                .isConfirmed(accommodation.getIsConfirmed())
                .isSelected(accommodation.getIsSelected())
                .phoneNumber(accommodation.getPhoneNumber())
                .email(accommodation.getEmail())
                .bookingUrl(accommodation.getBookingUrl())
                .memo(accommodation.getMemo())
                .createdBy(accommodation.getCreatedBy())
                .createdAt(accommodation.getCreatedAt())
                .updatedAt(accommodation.getUpdatedAt())
                .likeCount((int) likeCount)  // Builder에서 직접 설정
                .build();
        
        return dto;
    }

    /**
     * Accommodation을 DTO로 변환 (좋아요 정보 포함)
     */
    public AccommodationDto toDto(Accommodation accommodation, Long userId) {
        AccommodationDto dto = toDto(accommodation);
        if (dto != null && userId != null) {
            // userId가 있을 때만 isLiked 설정 (likeCount는 이미 toDto(accommodation)에서 설정됨)
            boolean isLiked = accommodationLikeRepository.existsByAccommodationIdAndUserId(
                    accommodation.getId(), userId);
            dto.setIsLiked(isLiked);
        }
        return dto;
    }
}

