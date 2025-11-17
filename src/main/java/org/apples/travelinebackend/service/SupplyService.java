package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.Supply;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.SupplyMapper;
import org.apples.travelinebackend.repository.SupplyRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final SupplyMapper supplyMapper;

    @Transactional
    public SupplyDto createSupply(CreateSupplyRequest request, User user) {
        log.info("준비물 생성 요청: travelPlanId={}, userId={}", request.getTravelPlanId(), user.getId());

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + request.getTravelPlanId()));

        if (!travelPlan.hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("준비물을 등록할 권한이 없습니다");
        }

        Supply supply = Supply.builder()
                .travelPlan(travelPlan)
                .text(request.getText())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .category(request.getCategory())
                .memo(request.getMemo())
                .checked(false)
                .build();

        Supply saved = supplyRepository.save(supply);
        log.info("준비물 생성 완료: supplyId={}", saved.getId());

        return supplyMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SupplyDto> getSuppliesByTravelPlan(Long travelPlanId, User user) {
        log.info("여행 계획별 준비물 목록 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("여행 계획에 접근할 권한이 없습니다");
        }

        List<Supply> supplies = supplyRepository.findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        return supplies.stream()
                .map(supplyMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SupplyDto updateSupply(Long supplyId, UpdateSupplyRequest request, User user) {
        log.info("준비물 수정 요청: supplyId={}, userId={}", supplyId, user.getId());

        Supply supply = supplyRepository.findByIdAndDeletedAtIsNull(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("준비물을 찾을 수 없습니다: " + supplyId));

        if (!supply.getTravelPlan().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 준비물만 수정할 수 있습니다");
        }

        // 부분 업데이트
        if (request.getText() != null) supply.setText(request.getText());
        if (request.getQuantity() != null) supply.setQuantity(request.getQuantity());
        if (request.getUnit() != null) supply.setUnit(request.getUnit());
        if (request.getCategory() != null) supply.setCategory(request.getCategory());
        if (request.getMemo() != null) supply.setMemo(request.getMemo());
        
        if (request.getChecked() != null) {
            supply.setChecked(request.getChecked());
            supply.setCheckedAt(request.getChecked() ? LocalDateTime.now() : null);
        }

        Supply updated = supplyRepository.save(supply);
        log.info("준비물 수정 완료: supplyId={}", supplyId);

        return supplyMapper.toDto(updated);
    }

    @Transactional
    public void deleteSupply(Long supplyId, User user) {
        log.info("준비물 삭제 요청: supplyId={}, userId={}", supplyId, user.getId());

        Supply supply = supplyRepository.findByIdAndDeletedAtIsNull(supplyId)
                .orElseThrow(() -> new ResourceNotFoundException("준비물을 찾을 수 없습니다: " + supplyId));

        if (!supply.getTravelPlan().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 준비물만 삭제할 수 있습니다");
        }

        supply.setDeletedAt(LocalDateTime.now());
        supplyRepository.save(supply);

        log.info("준비물 삭제 완료: supplyId={}", supplyId);
    }

    @Transactional(readOnly = true)
    public SupplySummaryDto getSupplySummary(Long travelPlanId, User user) {
        log.info("준비물 요약 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 여행 계획만 조회할 수 있습니다");
        }

        int total = supplyRepository.countByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        int checked = supplyRepository.countCheckedByTravelPlanId(travelPlanId);

        return SupplySummaryDto.builder()
                .total(total)
                .checked(checked)
                .build();
    }
}

