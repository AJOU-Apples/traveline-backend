package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Expense;
import org.apples.travelinebackend.entity.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    
    /**
     * 특정 장소의 모든 지출의 place 참조 해제
     */
    @Modifying
    @Query("UPDATE Expense e SET e.place = NULL WHERE e.place.id = :placeId")
    int clearPlaceReference(@Param("placeId") Long placeId);
    
    /**
     * 특정 장소의 모든 지출을 강제로 삭제 (하드 삭제)
     */
    @Modifying
    @Query("DELETE FROM Expense e WHERE e.place.id = :placeId")
    int hardDeleteByPlaceId(@Param("placeId") Long placeId);
    
    /**
     * 여행 계획의 모든 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByTravelPlanId(@Param("travelPlanId") Long travelPlanId);
    
    /**
     * 여행 계획의 모든 지출 조회 (권한 필터링 - SHARED 또는 본인 지출만)
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "AND (e.type = 'SHARED' OR e.paidBy.id = :userId) " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByTravelPlanIdWithVisibility(@Param("travelPlanId") Long travelPlanId,
                                                     @Param("userId") Long userId);
    
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
     * 여행 계획의 지출 조회 (타입별 필터 + 권한 필터링)
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelPlan.id = :travelPlanId " +
           "AND (:type IS NULL OR e.type = :type) " +
           "AND (e.type = 'SHARED' OR e.paidBy.id = :userId) " +
           "ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findByTravelPlanIdAndTypeWithVisibility(@Param("travelPlanId") Long travelPlanId,
                                                           @Param("type") ExpenseType type,
                                                           @Param("userId") Long userId);
    
    /**
     * 특정 장소의 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.place.id = :placeId " +
           "ORDER BY e.createdAt DESC")
    List<Expense> findByPlaceId(@Param("placeId") Long placeId);
    
    /**
     * 특정 장소의 지출 조회 (권한 필터링)
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.place.id = :placeId " +
           "AND (e.type = 'SHARED' OR e.paidBy.id = :userId) " +
           "ORDER BY e.createdAt DESC")
    List<Expense> findByPlaceIdWithVisibility(@Param("placeId") Long placeId,
                                               @Param("userId") Long userId);
    
    /**
     * 특정 날짜의 지출 조회
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelDay.id = :travelDayId " +
           "ORDER BY e.expenseTime, e.createdAt DESC")
    List<Expense> findByTravelDayId(@Param("travelDayId") Long travelDayId);
    
    /**
     * 특정 날짜의 지출 조회 (권한 필터링)
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.travelDay.id = :travelDayId " +
           "AND (e.type = 'SHARED' OR e.paidBy.id = :userId) " +
           "ORDER BY e.expenseTime, e.createdAt DESC")
    List<Expense> findByTravelDayIdWithVisibility(@Param("travelDayId") Long travelDayId,
                                                   @Param("userId") Long userId);
    
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

