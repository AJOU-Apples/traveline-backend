package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.PhotoVisibility;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMemoRequest {

    @NotBlank(message = "메모 내용은 필수입니다")
    private String content;
    
    private PhotoVisibility visibility; // SHARED 또는 PERSONAL
}

