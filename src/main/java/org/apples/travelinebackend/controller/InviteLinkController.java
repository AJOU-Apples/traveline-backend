package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.AcceptInviteByLinkRequest;
import org.apples.travelinebackend.dto.InviteLinkDto;
import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.InviteLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 초대 코드 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class InviteLinkController {

    private final InviteLinkService inviteLinkService;

    /**
     * 초대 코드 조회
     * GET /api/travel-plans/{planId}/invite-code
     * 권한: OWNER, EDITOR
     */
    @GetMapping("/{planId}/invite-code")
    public ResponseEntity<InviteLinkDto> getInviteCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("GET /api/travel-plans/{}/invite-code - userId={}", planId, currentUser.getId());
        
        InviteLinkDto inviteCode = inviteLinkService.getInviteCode(planId, currentUser.getId());
        return ResponseEntity.ok(inviteCode);
    }

    /**
     * 초대 코드 생성
     * POST /api/travel-plans/{planId}/invite-code
     * 권한: OWNER, EDITOR
     */
    @PostMapping("/{planId}/invite-code")
    public ResponseEntity<InviteLinkDto> generateInviteCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("POST /api/travel-plans/{}/invite-code - userId={}", planId, currentUser.getId());
        
        InviteLinkDto inviteCode = inviteLinkService.generateInviteLink(planId, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(inviteCode);
    }

    /**
     * 초대 코드 정보 조회 (비인증)
     * GET /api/travel-plans/invite/{code}
     * 응답: TravelPlanDto (제한된 정보만)
     */
    @GetMapping("/invite/{code}")
    public ResponseEntity<TravelPlanDto> getInviteInfo(
            @PathVariable String code) {
        log.info("GET /api/travel-plans/invite/{}", code);
        
        TravelPlanDto travelPlan = inviteLinkService.getInviteInfo(code.toUpperCase());
        return ResponseEntity.ok(travelPlan);
    }

    /**
     * 코드로 초대 수락
     * POST /api/travel-plans/invite/accept
     * 인증 필요
     */
    @PostMapping("/invite/accept")
    public ResponseEntity<MemberDto> acceptInviteByCode(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AcceptInviteByLinkRequest request) {
        log.info("POST /api/travel-plans/invite/accept - userId={}, code={}", 
                currentUser.getId(), request.getCode());
        
        MemberDto member = inviteLinkService.acceptInviteByCode(
                request.getCode().toUpperCase(), currentUser.getId());
        return ResponseEntity.ok(member);
    }

    /**
     * 초대 코드 무효화
     * DELETE /api/travel-plans/{planId}/invite-code
     * 권한: OWNER, EDITOR
     */
    @DeleteMapping("/{planId}/invite-code")
    public ResponseEntity<Void> revokeInviteCode(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("DELETE /api/travel-plans/{}/invite-code - userId={}", planId, currentUser.getId());
        
        inviteLinkService.revokeInviteLink(planId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}

