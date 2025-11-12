package org.apples.travelinebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryDto {
    
    private BigDecimal totalAmount;        // 전체 지출 총액
    private BigDecimal totalPersonal;      // 개인 지출 총액
    private BigDecimal totalShared;        // 공동 지출 총액
    
    private Integer expenseCount;          // 지출 건수
}

