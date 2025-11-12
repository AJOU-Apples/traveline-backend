package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDto {
    private Long id;
    
    @NotBlank(message = "도시 이름은 필수입니다")
    private String name;
    
    @NotNull(message = "국내/해외 여부는 필수입니다")
    private Boolean isInternational;
    
    private Double latitude;
    private Double longitude;
}

