package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.ExpenseDto;
import org.apples.travelinebackend.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {
    
    public ExpenseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }
        
        return ExpenseDto.builder()
                .id(expense.getId())
                .travelPlanId(expense.getTravelPlan() != null ? expense.getTravelPlan().getId() : null)
                .travelDayId(expense.getTravelDay() != null ? expense.getTravelDay().getId() : null)
                .dayNumber(expense.getTravelDay() != null ? expense.getTravelDay().getDayNumber() : null)
                .placeId(expense.getPlace() != null ? expense.getPlace().getId() : null)
                .paidById(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
                .paidByName(expense.getPaidBy() != null ? expense.getPaidBy().getNickname() : null)
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .currency(expense.getCurrency())
                .type(expense.getType())
                .splitWith(expense.getSplitWith())
                .splitAmount(expense.getSplitAmount())
                .isSettled(expense.getIsSettled())
                .receiptImage(expense.getReceiptImage())
                .memo(expense.getMemo())
                .expenseDate(expense.getExpenseDate())
                .expenseTime(expense.getExpenseTime())
                .createdAt(expense.getCreatedAt())
                .updatedAt(expense.getUpdatedAt())
                .build();
    }
}

