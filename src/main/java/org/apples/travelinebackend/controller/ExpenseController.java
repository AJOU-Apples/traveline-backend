package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateExpenseRequest;
import org.apples.travelinebackend.dto.ExpenseDto;
import org.apples.travelinebackend.dto.ExpenseSummaryDto;
import org.apples.travelinebackend.dto.UpdateExpenseRequest;
import org.apples.travelinebackend.entity.ExpenseType;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * 지출 추가
     * POST /api/expenses
     */
    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreateExpenseRequest request) {
        
        log.info("POST /api/expenses - userId={}, travelPlanId={}, amount={}", 
                currentUser.getId(), request.getTravelPlanId(), request.getAmount());

        ExpenseDto createdExpense = expenseService.createExpense(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    /**
     * 여행 계획의 모든 지출 조회
     * GET /api/expenses?travelPlanId={planId}&type={personal|shared}
     */
    @GetMapping(params = "travelPlanId")
    public ResponseEntity<Map<String, Object>> getExpensesByTravelPlan(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId,
            @RequestParam(required = false) String type) {
        
        log.info("GET /api/expenses?travelPlanId={}&type={} - userId={}", 
                travelPlanId, type, currentUser.getId());

        ExpenseType expenseType = type != null ? ExpenseType.valueOf(type.toUpperCase()) : null;
        List<ExpenseDto> expenses = expenseService.getExpensesByTravelPlan(
                travelPlanId, expenseType, currentUser.getId());

        // 통계도 함께 반환
        ExpenseSummaryDto summary = expenseService.getExpenseSummary(travelPlanId, currentUser.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("expenses", expenses);
        response.put("summary", summary);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 장소의 지출 조회
     * GET /api/expenses?placeId={placeId}
     */
    @GetMapping(params = "placeId")
    public ResponseEntity<Map<String, List<ExpenseDto>>> getExpensesByPlace(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long placeId) {
        
        log.info("GET /api/expenses?placeId={} - userId={}", placeId, currentUser.getId());

        List<ExpenseDto> expenses = expenseService.getExpensesByPlace(placeId, currentUser.getId());

        Map<String, List<ExpenseDto>> response = new HashMap<>();
        response.put("expenses", expenses);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 날짜의 지출 조회
     * GET /api/expenses?travelPlanId={planId}&dayNumber={dayNumber}
     */
    @GetMapping(params = {"travelPlanId", "dayNumber"})
    public ResponseEntity<Map<String, List<ExpenseDto>>> getExpensesByDay(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId,
            @RequestParam Integer dayNumber) {
        
        log.info("GET /api/expenses?travelPlanId={}&dayNumber={} - userId={}", 
                travelPlanId, dayNumber, currentUser.getId());

        List<ExpenseDto> expenses = expenseService.getExpensesByDay(
                travelPlanId, dayNumber, currentUser.getId());

        Map<String, List<ExpenseDto>> response = new HashMap<>();
        response.put("expenses", expenses);

        return ResponseEntity.ok(response);
    }

    /**
     * 지출 상세 조회
     * GET /api/expenses/{expenseId}
     */
    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> getExpense(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long expenseId) {
        
        log.info("GET /api/expenses/{} - userId={}", expenseId, currentUser.getId());

        ExpenseDto expense = expenseService.getExpenseById(expenseId, currentUser.getId());
        return ResponseEntity.ok(expense);
    }

    /**
     * 지출 수정
     * PUT /api/expenses/{expenseId}
     */
    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseDto> updateExpense(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long expenseId,
            @Valid @RequestBody UpdateExpenseRequest request) {
        
        log.info("PUT /api/expenses/{} - userId={}", expenseId, currentUser.getId());

        ExpenseDto updatedExpense = expenseService.updateExpense(
                expenseId, request, currentUser.getId());
        return ResponseEntity.ok(updatedExpense);
    }

    /**
     * 지출 삭제
     * DELETE /api/expenses/{expenseId}
     */
    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long expenseId) {
        
        log.info("DELETE /api/expenses/{} - userId={}", expenseId, currentUser.getId());

        expenseService.deleteExpense(expenseId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 지출 통계 조회
     * GET /api/expenses/summary?travelPlanId={planId}
     */
    @GetMapping("/summary")
    public ResponseEntity<ExpenseSummaryDto> getExpenseSummary(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long travelPlanId) {
        
        log.info("GET /api/expenses/summary?travelPlanId={} - userId={}", 
                travelPlanId, currentUser.getId());

        ExpenseSummaryDto summary = expenseService.getExpenseSummary(
                travelPlanId, currentUser.getId());
        return ResponseEntity.ok(summary);
    }
}

