package org.apples.travelinebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apples.travelinebackend.entity.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {
    
    @NotNull(message = "여행 계획 ID는 필수입니다.")
    private Long travelPlanId;
    
    private Integer dayNumber;  // optional
    private Long placeId;       // optional
    
    @NotBlank(message = "지출 제목은 필수입니다.")
    private String title;
    
    @NotNull(message = "금액은 필수입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private BigDecimal amount;
    
    private String currency;  // default: KRW
    
    @NotNull(message = "지출 타입은 필수입니다.")
    private ExpenseType type;  // PERSONAL | SHARED
    
    // SHARED인 경우 정산할 사람들 (userId 목록)
    private List<Long> splitWith;
    
    private String memo;
    
    private LocalDate expenseDate;
    private String expenseTime;
}

