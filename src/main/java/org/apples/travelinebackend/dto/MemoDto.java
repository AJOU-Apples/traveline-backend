package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.PhotoVisibility;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoDto {
    private Long id;
    private Long placeId;
    private AuthUserDto author;
    private String content;
    private PhotoVisibility visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

