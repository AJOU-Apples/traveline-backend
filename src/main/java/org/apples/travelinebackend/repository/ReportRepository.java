package org.apples.travelinebackend.repository;

import org.apples.travelinebackend.entity.Report;
import org.apples.travelinebackend.entity.ReportStatus;
import org.apples.travelinebackend.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 같은 사용자가 같은 대상을 이미 신고했는지 확인
     */
    boolean existsByReporterIdAndTargetTypeAndTargetId(
            Long reporterId, 
            ReportTargetType targetType, 
            Long targetId
    );

    /**
     * 신고 조회 (중복 체크용)
     */
    Optional<Report> findByReporterIdAndTargetTypeAndTargetId(
            Long reporterId,
            ReportTargetType targetType,
            Long targetId
    );

    /**
     * 신고 목록 조회 (필터링)
     */
    @Query("SELECT r FROM Report r " +
           "LEFT JOIN FETCH r.reporter " +
           "WHERE (:status IS NULL OR r.status = :status) AND " +
           "(:targetType IS NULL OR r.targetType = :targetType) " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findByFilters(
            @Param("status") ReportStatus status,
            @Param("targetType") ReportTargetType targetType,
            Pageable pageable
    );

    /**
     * 신고 목록 조회 (PROCESSED 상태 = REVIEWED 또는 REJECTED)
     */
    @Query("SELECT r FROM Report r " +
           "LEFT JOIN FETCH r.reporter " +
           "WHERE r.status IN ('REVIEWED', 'REJECTED') AND " +
           "(:targetType IS NULL OR r.targetType = :targetType) " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findProcessedReports(
            @Param("targetType") ReportTargetType targetType,
            Pageable pageable
    );

    /**
     * 특정 대상에 대한 신고 개수 조회
     */
    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);
}

