package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.AccommodationDto;
import org.apples.travelinebackend.entity.Accommodation;
import org.springframework.stereotype.Component;

@Component
public class AccommodationMapper {

    public AccommodationDto toDto(Accommodation accommodation) {
        if (accommodation == null) {
            return null;
        }

        return AccommodationDto.builder()
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
                .build();
    }
}

