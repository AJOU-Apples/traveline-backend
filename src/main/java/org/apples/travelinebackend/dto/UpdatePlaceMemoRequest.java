package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlaceMemoRequest {
    
    @NotBlank(message = "메모 타입은 필수입니다.")
    private String type;  // "shared" | "personal"
    
    private String memo;  // 메모 내용 (빈 문자열이면 삭제)
}


