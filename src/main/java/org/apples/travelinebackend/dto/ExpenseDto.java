package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExpenseDto {
    
    private Long id;
    private Long travelPlanId;
    private Long travelDayId;
    private Integer dayNumber;
    private Long placeId;
    
    // 결제 정보
    private Long paidById;
    private String paidByName;
    
    // 지출 정보
    private String title;
    private BigDecimal amount;
    private String currency;
    
    // 지출 타입
    private ExpenseType type;
    
    // 정산 정보
    private List<Long> splitWith;
    private BigDecimal splitAmount;
    private Boolean isSettled;
    
    // 영수증
    private String receiptImage;
    
    // 메모
    private String memo;
    
    // 날짜 및 시간
    private LocalDate expenseDate;
    private String expenseTime;
    
    // 타임스탬프
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

