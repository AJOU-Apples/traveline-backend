package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);

    /**
     * 월간 활성 사용자 수 (MAU) - 특정 월에 활동한 사용자
     */
    @Query("SELECT COUNT(DISTINCT u.id) FROM User u " +
           "WHERE u.lastLoginAt >= :startDate AND u.lastLoginAt < :endDate " +
           "OR (u.createdAt >= :startDate AND u.createdAt < :endDate)")
    long countActiveUsersInPeriod(@Param("startDate") LocalDateTime startDate, 
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 기간 동안 가입한 신규 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    long countNewUsersInPeriod(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
}

