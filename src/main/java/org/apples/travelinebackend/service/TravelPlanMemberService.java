package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.InviteMemberRequest;
import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.dto.UpdateMemberRoleRequest;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TravelPlanMemberMapper;
import org.apples.travelinebackend.repository.TravelPlanMemberRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.apples.travelinebackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelPlanMemberService {

    private final TravelPlanMemberRepository memberRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final UserRepository userRepository;
    private final TravelPlanMemberMapper memberMapper;

    /**
     * 여행 계획의 모든 멤버 조회
     */
    public List<MemberDto> getMembers(Long travelPlanId, Long currentUserId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - 멤버만 멤버 목록 조회 가능
        if (!travelPlan.hasAccess(currentUserId)) {
            throw new ForbiddenException("해당 여행 계획의 멤버가 아닙니다.");
        }

        List<TravelPlanMember> members = memberRepository.findByTravelPlanIdWithUser(travelPlanId);
        return members.stream()
                .map(memberMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 멤버 초대
     */
    @Transactional
    public MemberDto inviteMember(Long travelPlanId, InviteMemberRequest request, Long inviterId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - OWNER만 멤버 초대 가능
        if (!travelPlan.hasRole(inviterId, MemberRole.OWNER)) {
            throw new ForbiddenException("멤버를 초대할 권한이 없습니다. (소유자만 가능)");
        }

        // 초대할 사용자 조회
        User invitee = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "email", request.getEmail()));

        // 이미 멤버인지 확인
        if (memberRepository.findByTravelPlanIdAndUserId(travelPlanId, invitee.getId()).isPresent()) {
            throw new BadRequestException("이미 이 여행 계획의 멤버입니다.");
        }

        // OWNER 역할은 초대 불가
        if (request.getRole() == MemberRole.OWNER) {
            throw new BadRequestException("OWNER 역할로 초대할 수 없습니다.");
        }

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "id", inviterId));

        // 멤버 생성
        TravelPlanMember member = TravelPlanMember.builder()
                .travelPlan(travelPlan)
                .user(invitee)
                .role(request.getRole())
                .status(InvitationStatus.PENDING)
                .invitedBy(inviter)
                .build();

        TravelPlanMember savedMember = memberRepository.save(member);
        log.info("여행 계획 멤버 초대: travelPlanId={}, inviteeId={}, role={}", 
                travelPlanId, invitee.getId(), request.getRole());

        return memberMapper.toDto(savedMember);
    }

    /**
     * 초대 수락
     */
    @Transactional
    public MemberDto acceptInvitation(Long travelPlanId, Long userId) {
        TravelPlanMember member = memberRepository.findByTravelPlanIdAndUserId(travelPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("초대", "travelPlanId", travelPlanId));

        if (member.getStatus() == InvitationStatus.ACCEPTED) {
            throw new BadRequestException("이미 수락한 초대입니다.");
        }

        if (member.getStatus() == InvitationStatus.REJECTED) {
            throw new BadRequestException("거절한 초대는 수락할 수 없습니다.");
        }

        member.setStatus(InvitationStatus.ACCEPTED);
        member.setJoinedAt(LocalDateTime.now());

        TravelPlanMember updatedMember = memberRepository.save(member);
        log.info("여행 계획 초대 수락: travelPlanId={}, userId={}", travelPlanId, userId);

        return memberMapper.toDto(updatedMember);
    }

    /**
     * 초대 거절
     */
    @Transactional
    public void rejectInvitation(Long travelPlanId, Long userId) {
        TravelPlanMember member = memberRepository.findByTravelPlanIdAndUserId(travelPlanId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("초대", "travelPlanId", travelPlanId));

        if (member.getStatus() == InvitationStatus.ACCEPTED) {
            throw new BadRequestException("이미 수락한 초대는 거절할 수 없습니다.");
        }

        member.setStatus(InvitationStatus.REJECTED);
        memberRepository.save(member);
        
        log.info("여행 계획 초대 거절: travelPlanId={}, userId={}", travelPlanId, userId);
    }

    /**
     * 멤버 역할 변경
     */
    @Transactional
    public MemberDto updateMemberRole(Long travelPlanId, Long targetUserId, 
                                     UpdateMemberRoleRequest request, Long currentUserId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - OWNER만 역할 변경 가능
        if (!travelPlan.hasRole(currentUserId, MemberRole.OWNER)) {
            throw new ForbiddenException("멤버 역할을 변경할 권한이 없습니다. (소유자만 가능)");
        }

        TravelPlanMember member = memberRepository.findByTravelPlanIdAndUserId(travelPlanId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("멤버", "userId", targetUserId));

        // OWNER 역할로 변경 불가
        if (request.getRole() == MemberRole.OWNER) {
            throw new BadRequestException("OWNER 역할로 변경할 수 없습니다.");
        }

        // 자신의 역할은 변경 불가 (OWNER가 자신을 강등하는 것 방지)
        if (member.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("자신의 역할은 변경할 수 없습니다.");
        }

        // OWNER의 역할은 변경 불가
        if (member.getRole() == MemberRole.OWNER) {
            throw new BadRequestException("소유자의 역할은 변경할 수 없습니다.");
        }

        member.setRole(request.getRole());
        TravelPlanMember updatedMember = memberRepository.save(member);
        
        log.info("여행 계획 멤버 역할 변경: travelPlanId={}, targetUserId={}, newRole={}", 
                travelPlanId, targetUserId, request.getRole());

        return memberMapper.toDto(updatedMember);
    }

    /**
     * 멤버 제거
     */
    @Transactional
    public void removeMember(Long travelPlanId, Long targetUserId, Long currentUserId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        TravelPlanMember member = memberRepository.findByTravelPlanIdAndUserId(travelPlanId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("멤버", "userId", targetUserId));

        // OWNER는 제거 불가
        if (member.getRole() == MemberRole.OWNER) {
            throw new BadRequestException("소유자는 제거할 수 없습니다.");
        }

        // 권한 확인: OWNER는 누구나 제거 가능, 본인은 자신을 제거 가능
        boolean isOwner = travelPlan.hasRole(currentUserId, MemberRole.OWNER);
        boolean isSelf = targetUserId.equals(currentUserId);

        if (!isOwner && !isSelf) {
            throw new ForbiddenException("멤버를 제거할 권한이 없습니다.");
        }

        memberRepository.delete(member);
        log.info("여행 계획 멤버 제거: travelPlanId={}, targetUserId={}, removedBy={}", 
                travelPlanId, targetUserId, currentUserId);
    }

    /**
     * 사용자의 모든 초대 목록 조회 (대기중)
     */
    public List<MemberDto> getPendingInvitations(Long userId) {
        List<TravelPlanMember> invitations = memberRepository.findByUserIdAndStatus(
                userId, InvitationStatus.PENDING);
        
        return invitations.stream()
                .map(memberMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자가 참여중인 여행 계획 목록
     */
    public List<MemberDto> getMyMemberships(Long userId) {
        List<TravelPlanMember> memberships = memberRepository.findAcceptedMembershipsByUserId(userId);
        
        return memberships.stream()
                .map(memberMapper::toDto)
                .collect(Collectors.toList());
    }
}

