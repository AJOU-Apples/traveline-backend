package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSupplyRequest {

    // 부분 업데이트 지원: 모든 필드 선택사항
    private String text;
    private Integer quantity;
    private String unit;
    private String category;
    private String memo;
    private Boolean checked;
}

