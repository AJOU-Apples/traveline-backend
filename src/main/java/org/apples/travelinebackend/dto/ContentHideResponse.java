package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentHideResponse {
    private Long id;
    private Boolean isHidden;
    private LocalDateTime hiddenAt;
    private UserDto hiddenBy;
}

