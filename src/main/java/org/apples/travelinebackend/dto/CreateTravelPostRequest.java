package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ExpenseDisplayType;
import org.apples.travelinebackend.entity.MemoDisplayType;
import org.apples.travelinebackend.entity.PostVisibility;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTravelPostRequest {

    @NotNull(message = "여행 계획 ID는 필수입니다")
    private Long travelPlanId;

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String content;

    private String coverImageUrl;

    @NotNull(message = "공개 설정은 필수입니다")
    private PostVisibility visibility;

    private ExpenseDisplayType expenseDisplayType;

    private MemoDisplayType memoDisplayType;

    @Builder.Default
    private List<Long> selectedPhotoIds = new ArrayList<>();
}

