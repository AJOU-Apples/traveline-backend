package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CreateExpenseRequest;
import org.apples.travelinebackend.dto.ExpenseDto;
import org.apples.travelinebackend.dto.ExpenseSummaryDto;
import org.apples.travelinebackend.dto.UpdateExpenseRequest;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.ExpenseMapper;
import org.apples.travelinebackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelDayRepository travelDayRepository;
    private final PlaceRepository placeRepository;
    private final ExpenseMapper expenseMapper;

    /**
     * 지출 추가
     */
    @Transactional
    public ExpenseDto createExpense(CreateExpenseRequest request, User user) {
        // TravelPlan 조회 및 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", request.getTravelPlanId()));

        if (!travelPlan.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // TravelDay 조회 (optional)
        TravelDay travelDay = null;
        if (request.getDayNumber() != null) {
            travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(
                    request.getTravelPlanId(), request.getDayNumber())
                    .orElse(null);
        }

        // Place 조회 (optional)
        Place place = null;
        if (request.getPlaceId() != null) {
            place = placeRepository.findById(request.getPlaceId())
                    .orElse(null);
        }

        // splitAmount 계산 (SHARED인 경우)
        BigDecimal splitAmount = null;
        if (request.getType() == ExpenseType.SHARED && 
            request.getSplitWith() != null && !request.getSplitWith().isEmpty()) {
            int splitCount = request.getSplitWith().size() + 1;  // splitWith + paidBy
            splitAmount = request.getAmount().divide(
                    BigDecimal.valueOf(splitCount), 2, RoundingMode.HALF_UP);
        }

        // 통화 자동 설정: 요청 > 목적지 통화 > 기본값(KRW)
        String currency = request.getCurrency();
        if (currency == null || currency.isEmpty()) {
            if (travelPlan.getDestination() != null && travelPlan.getDestination().getCurrency() != null) {
                currency = travelPlan.getDestination().getCurrency();
            } else {
                currency = "KRW";  // 기본값
            }
        }

        // Expense 생성
        Expense expense = Expense.builder()
                .travelPlan(travelPlan)
                .travelDay(travelDay)
                .place(place)
                .paidBy(user)
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(currency)
                .type(request.getType())
                .splitWith(request.getSplitWith())
                .splitAmount(splitAmount)
                .isSettled(false)
                .memo(request.getMemo())
                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate() : LocalDate.now())
                .expenseTime(request.getExpenseTime())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        log.info("지출 추가 완료: expenseId={}, userId={}, amount={}", 
                savedExpense.getId(), user.getId(), savedExpense.getAmount());

        return expenseMapper.toDto(savedExpense);
    }

    /**
     * 여행 계획의 모든 지출 조회
     */
    public List<ExpenseDto> getExpensesByTravelPlan(Long travelPlanId, ExpenseType type, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        List<Expense> expenses = expenseRepository.findByTravelPlanIdAndType(travelPlanId, type);
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 장소의 지출 조회
     */
    public List<ExpenseDto> getExpensesByPlace(Long placeId, Long userId) {
        // Place 조회 및 권한 검증
        Place place = placeRepository.findByIdWithTravelPlan(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("장소", "id", placeId));

        if (!place.getTravelDay().getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 장소에 대한 권한이 없습니다.");
        }

        List<Expense> expenses = expenseRepository.findByPlaceId(placeId);
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 날짜의 지출 조회
     */
    public List<ExpenseDto> getExpensesByDay(Long travelPlanId, Integer dayNumber, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // TravelDay 조회
        TravelDay travelDay = travelDayRepository.findByTravelPlanIdAndDayNumber(travelPlanId, dayNumber)
                .orElseThrow(() -> new ResourceNotFoundException("여행 일차", "dayNumber", dayNumber));

        List<Expense> expenses = expenseRepository.findByTravelDayId(travelDay.getId());
        return expenses.stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 지출 상세 조회
     */
    public ExpenseDto getExpenseById(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findByIdWithTravelPlan(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("지출", "id", expenseId));

        // 권한 검증
        if (!expense.getTravelPlan().getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 지출에 대한 권한이 없습니다.");
        }

        return expenseMapper.toDto(expense);
    }

    /**
     * 지출 수정
     */
    @Transactional
    public ExpenseDto updateExpense(Long expenseId, UpdateExpenseRequest request, Long userId) {
        Expense expense = expenseRepository.findByIdWithTravelPlan(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("지출", "id", expenseId));

        // 권한 검증 (결제한 사람만 수정 가능)
        if (!expense.getPaidBy().getId().equals(userId)) {
            throw new ForbiddenException("지출을 수정할 권한이 없습니다.");
        }

        // 필드 업데이트
        if (request.getTitle() != null) {
            expense.setTitle(request.getTitle());
        }
        if (request.getAmount() != null) {
            expense.setAmount(request.getAmount());
            
            // splitAmount 재계산
            if (expense.getType() == ExpenseType.SHARED && 
                expense.getSplitWith() != null && !expense.getSplitWith().isEmpty()) {
                int splitCount = expense.getSplitWith().size() + 1;
                expense.setSplitAmount(request.getAmount().divide(
                        BigDecimal.valueOf(splitCount), 2, RoundingMode.HALF_UP));
            }
        }
        if (request.getType() != null) {
            expense.setType(request.getType());
        }
        if (request.getSplitWith() != null) {
            expense.setSplitWith(request.getSplitWith());
            
            // splitAmount 재계산
            if (!request.getSplitWith().isEmpty()) {
                int splitCount = request.getSplitWith().size() + 1;
                expense.setSplitAmount(expense.getAmount().divide(
                        BigDecimal.valueOf(splitCount), 2, RoundingMode.HALF_UP));
            }
        }
        if (request.getIsSettled() != null) {
            expense.setIsSettled(request.getIsSettled());
        }
        if (request.getMemo() != null) {
            expense.setMemo(request.getMemo());
        }
        if (request.getExpenseDate() != null) {
            expense.setExpenseDate(request.getExpenseDate());
        }
        if (request.getExpenseTime() != null) {
            expense.setExpenseTime(request.getExpenseTime());
        }

        Expense updatedExpense = expenseRepository.save(expense);
        log.info("지출 수정 완료: expenseId={}, userId={}", expenseId, userId);

        return expenseMapper.toDto(updatedExpense);
    }

    /**
     * 지출 삭제
     */
    @Transactional
    public void deleteExpense(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findByIdWithTravelPlan(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("지출", "id", expenseId));

        // 권한 검증 (결제한 사람 또는 여행 계획 소유자만 삭제 가능)
        boolean isOwner = expense.getTravelPlan().getUser().getId().equals(userId);
        boolean isPayer = expense.getPaidBy().getId().equals(userId);

        if (!isOwner && !isPayer) {
            throw new ForbiddenException("지출을 삭제할 권한이 없습니다.");
        }

        expenseRepository.delete(expense);
        log.info("지출 삭제 완료: expenseId={}, userId={}", expenseId, userId);
    }

    /**
     * 지출 통계 조회
     */
    public ExpenseSummaryDto getExpenseSummary(Long travelPlanId, Long userId) {
        // TravelPlan 권한 검증
        TravelPlan travelPlan = travelPlanRepository.findById(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        if (!travelPlan.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 여행 계획에 대한 권한이 없습니다.");
        }

        // 전체 지출 조회
        List<Expense> allExpenses = expenseRepository.findByTravelPlanId(travelPlanId);

        // 총액 계산
        BigDecimal totalAmount = expenseRepository.sumAmountByTravelPlanId(travelPlanId);
        BigDecimal totalPersonal = expenseRepository.sumAmountByTravelPlanIdAndType(
                travelPlanId, ExpenseType.PERSONAL);
        BigDecimal totalShared = expenseRepository.sumAmountByTravelPlanIdAndType(
                travelPlanId, ExpenseType.SHARED);

        return ExpenseSummaryDto.builder()
                .totalAmount(totalAmount)
                .totalPersonal(totalPersonal)
                .totalShared(totalShared)
                .expenseCount(allExpenses.size())
                .build();
    }
}

