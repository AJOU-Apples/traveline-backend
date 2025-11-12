package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Expense;
import org.apples.travelinebackend.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    /**
     * 여행 계획의 모든 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
    
    /**
     * 여행 계획의 지출 조회 (타입별 필터)
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "AND (:type IS NULL OR e.type = :type) " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByTravelPlanIdAndType(@Param("travelPlanId") Long travelPlanId,
                                             @Param("type") ExpenseType type);
    
    /**
     * 특정 장소의 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.place.id = :placeId " +
           "ORDER BY e.createdAt DESC")
    List<Expense> findByPlaceId(@Param("placeId") Long placeId);
    
    /**
     * 특정 날짜의 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelDay.id = :travelDayId " +
           "ORDER BY e.expenseTime, e.createdAt DESC")
    List<Expense> findByTravelDayId(@Param("travelDayId") Long travelDayId);
    
    /**
     * 지출 조회 (TravelPlan과 함께 - 권한 검증용)
     */
    @Query("SELECT e FROM Expense e " +
           "JOIN FETCH e.travelPlan tp " +
           "WHERE e.id = :expenseId")
    Optional<Expense> findByIdWithTravelPlan(@Param("expenseId") Long expenseId);
    
    /**
     * 여행 계획의 총 지출 금액
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId")
    BigDecimal sumAmountByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
    
    /**
     * 여행 계획의 타입별 총 지출 금액
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "AND e.type = :type")
    BigDecimal sumAmountByTravelPlanIdAndType(@Param("travelPlanId") Long travelPlanId,
                                               @Param("type") ExpenseType type);
}

