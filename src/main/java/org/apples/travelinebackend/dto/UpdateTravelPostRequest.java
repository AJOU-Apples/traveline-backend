package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ExpenseDisplayType;
import org.apples.travelinebackend.entity.MemoDisplayType;
import org.apples.travelinebackend.entity.PostVisibility;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTravelPostRequest {

    private String title;

    private String content;

    private String coverImageUrl;

    private PostVisibility visibility;

    private ExpenseDisplayType expenseDisplayType;

    private MemoDisplayType memoDisplayType;

    private List<Long> selectedPhotoIds;
}

