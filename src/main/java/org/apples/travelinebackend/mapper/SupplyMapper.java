package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.SupplyDto;
import org.apples.travelinebackend.entity.Supply;
import org.springframework.stereotype.Component;

@Component
public class SupplyMapper {

    public SupplyDto toDto(Supply supply) {
        if (supply == null) {
            return null;
        }

        return SupplyDto.builder()
                .id(supply.getId())
                .travelPlanId(supply.getTravelPlan().getId())
                .text(supply.getText())
                .quantity(supply.getQuantity())
                .unit(supply.getUnit())
                .category(supply.getCategory())
                .memo(supply.getMemo())
                .checked(supply.getChecked())
                .checkedAt(supply.getCheckedAt())
                .createdAt(supply.getCreatedAt())
                .updatedAt(supply.getUpdatedAt())
                .build();
    }
}

