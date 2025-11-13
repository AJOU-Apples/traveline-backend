package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.InviteMemberRequest;
import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.dto.UpdateMemberRoleRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.TravelPlanMemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 여행 계획 멤버 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/travel-plans")
@RequiredArgsConstructor
public class TravelPlanMemberController {

    private final TravelPlanMemberService memberService;

    /**
     * 여행 계획의 멤버 목록 조회
     * GET /api/travel-plans/{planId}/members
     */
    @GetMapping("/{planId}/members")
    public ResponseEntity<Map<String, List<MemberDto>>> getMembers(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("GET /api/travel-plans/{}/members - userId={}", planId, currentUser.getId());
        
        List<MemberDto> members = memberService.getMembers(planId, currentUser.getId());
        
        Map<String, List<MemberDto>> response = new HashMap<>();
        response.put("members", members);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 멤버 초대
     * POST /api/travel-plans/{planId}/members/invite
     */
    @PostMapping("/{planId}/members/invite")
    public ResponseEntity<MemberDto> inviteMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId,
            @Valid @RequestBody InviteMemberRequest request) {
        log.info("POST /api/travel-plans/{}/members/invite - userId={}, email={}, role={}", 
                planId, currentUser.getId(), request.getEmail(), request.getRole());
        
        MemberDto member = memberService.inviteMember(planId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    /**
     * 초대 수락
     * POST /api/travel-plans/{planId}/members/accept
     */
    @PostMapping("/{planId}/members/accept")
    public ResponseEntity<MemberDto> acceptInvitation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("POST /api/travel-plans/{}/members/accept - userId={}", planId, currentUser.getId());
        
        MemberDto member = memberService.acceptInvitation(planId, currentUser.getId());
        return ResponseEntity.ok(member);
    }

    /**
     * 초대 거절
     * POST /api/travel-plans/{planId}/members/reject
     */
    @PostMapping("/{planId}/members/reject")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId) {
        log.info("POST /api/travel-plans/{}/members/reject - userId={}", planId, currentUser.getId());
        
        memberService.rejectInvitation(planId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 멤버 역할 변경
     * PUT /api/travel-plans/{planId}/members/{userId}/role
     */
    @PutMapping("/{planId}/members/{userId}/role")
    public ResponseEntity<MemberDto> updateMemberRole(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateMemberRoleRequest request) {
        log.info("PUT /api/travel-plans/{}/members/{}/role - currentUserId={}, newRole={}", 
                planId, userId, currentUser.getId(), request.getRole());
        
        MemberDto member = memberService.updateMemberRole(planId, userId, request, currentUser.getId());
        return ResponseEntity.ok(member);
    }

    /**
     * 멤버 제거 (또는 탈퇴)
     * DELETE /api/travel-plans/{planId}/members/{userId}
     */
    @DeleteMapping("/{planId}/members/{userId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long planId,
            @PathVariable Long userId) {
        log.info("DELETE /api/travel-plans/{}/members/{} - currentUserId={}", 
                planId, userId, currentUser.getId());
        
        memberService.removeMember(planId, userId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 초대 목록 조회 (대기중인 초대만)
     * GET /api/travel-plans/invitations
     */
    @GetMapping("/invitations")
    public ResponseEntity<Map<String, List<MemberDto>>> getMyInvitations(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/travel-plans/invitations - userId={}", currentUser.getId());
        
        List<MemberDto> invitations = memberService.getPendingInvitations(currentUser.getId());
        
        Map<String, List<MemberDto>> response = new HashMap<>();
        response.put("invitations", invitations);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 참여중인 여행 계획 멤버십 목록
     * GET /api/travel-plans/memberships
     */
    @GetMapping("/memberships")
    public ResponseEntity<Map<String, List<MemberDto>>> getMyMemberships(
            @AuthenticationPrincipal User currentUser) {
        log.info("GET /api/travel-plans/memberships - userId={}", currentUser.getId());
        
        List<MemberDto> memberships = memberService.getMyMemberships(currentUser.getId());
        
        Map<String, List<MemberDto>> response = new HashMap<>();
        response.put("memberships", memberships);
        
        return ResponseEntity.ok(response);
    }
}

