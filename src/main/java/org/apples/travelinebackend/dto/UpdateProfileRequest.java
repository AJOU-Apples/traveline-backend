package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다")
    private String name;

    @Size(min = 2, max = 50, message = "닉네임은 2-50자 사이여야 합니다")
    private String username;

    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다")
    private String profileImage;

    @Size(max = 500, message = "소개는 500자를 초과할 수 없습니다")
    private String bio;
}

