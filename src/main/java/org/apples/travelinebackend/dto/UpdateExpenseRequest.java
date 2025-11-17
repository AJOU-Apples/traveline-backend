package org.apples.travelinebackend.dto;

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
public class UpdateExpenseRequest {
    
    private String title;
    private BigDecimal amount;
    private ExpenseType type;
    private List<Long> splitWith;
    private Boolean isSettled;
    private String memo;
    private LocalDate expenseDate;
    private String expenseTime;
}

