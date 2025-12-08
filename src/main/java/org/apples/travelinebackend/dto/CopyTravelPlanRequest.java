package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CopyTravelPlanRequest {
    
    /**
     * 복사할 여행 계획 제목 (선택적)
     * null이면 "원본제목 복사본" 형식으로 자동 생성
     */
    private String title;
    
    /**
     * 시작일 (선택적)
     * null이면 원본과 동일한 기간으로 설정 (오늘부터 시작)
     */
    private String startDate;
    
    /**
     * 종료일 (선택적)
     * null이면 원본과 동일한 기간으로 설정
     */
    private String endDate;
}

