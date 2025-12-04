package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.InviteLinkDto;
import org.apples.travelinebackend.dto.MemberDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.*;
import org.apples.travelinebackend.exception.BadRequestException;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.mapper.TravelPlanMemberMapper;
import org.apples.travelinebackend.repository.InviteLinkRepository;
import org.apples.travelinebackend.repository.TravelPlanMemberRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.apples.travelinebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteLinkService {

    private final InviteLinkRepository inviteLinkRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TravelPlanMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final TravelPlanMapper travelPlanMapper;
    private final TravelPlanMemberMapper memberMapper;

    @Value("${app.invite-link.expiration-days:7}")
    private int expirationDays;

    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRIES = 10;
    private final SecureRandom random = new SecureRandom();

    /**
     * 6자리 초대 코드 생성 (중복 체크 포함)
     */
    private String generateInviteCode() {
        for (int i = 0; i < MAX_RETRIES; i++) {
            StringBuilder code = new StringBuilder(CODE_LENGTH);
            for (int j = 0; j < CODE_LENGTH; j++) {
                code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            String generatedCode = code.toString();

            // 중복 체크
            if (!inviteLinkRepository.findByCode(generatedCode).isPresent()) {
                return generatedCode;
            }
        }
        throw new RuntimeException("초대 코드 생성에 실패했습니다. 재시도해주세요.");
    }

    /**
     * 초대 코드 생성
     */
    @Transactional
    public InviteLinkDto generateInviteLink(Long travelPlanId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - OWNER 또는 EDITOR만 코드 생성 가능
        if (!travelPlan.hasRole(userId, MemberRole.OWNER) &&
                !travelPlan.hasRole(userId, MemberRole.EDITOR)) {
            throw new ForbiddenException("초대 코드를 생성할 권한이 없습니다. (소유자 또는 편집자만 가능)");
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "id", userId));

        // 기존 활성화되고 유효한 코드가 있는지 확인
        List<InviteLink> existingLinks = inviteLinkRepository.findByTravelPlanId(travelPlanId);
        LocalDateTime now = LocalDateTime.now();

        InviteLink activeLink = existingLinks.stream()
                .filter(link -> link.getIsActive() && link.getExpiresAt().isAfter(now))
                .findFirst()
                .orElse(null);

        // 기존 활성화된 유효한 코드가 있으면 그대로 반환
        if (activeLink != null) {
            log.info("기존 초대 코드 반환: travelPlanId={}, userId={}, code={}",
                    travelPlanId, userId, activeLink.getInviteCode());

            return InviteLinkDto.builder()
                    .code(activeLink.getInviteCode())
                    .expiresAt(activeLink.getExpiresAt())
                    .build();
        }

        // 기존 코드가 없거나 만료된 경우 새로 생성
        // 만료된 코드들은 비활성화
        existingLinks.stream()
                .filter(link -> link.getIsActive() && link.getExpiresAt().isBefore(now))
                .forEach(link -> link.setIsActive(false));
        inviteLinkRepository.saveAll(existingLinks);

        // 새 코드 생성
        LocalDateTime expiresAt = now.plusDays(expirationDays);
        String inviteCode = generateInviteCode();

        InviteLink inviteLink = InviteLink.builder()
                .travelPlan(travelPlan)
                .inviteCode(inviteCode)
                .createdBy(creator)
                .expiresAt(expiresAt)
                .isActive(true)
                .build();

        InviteLink savedLink = inviteLinkRepository.save(inviteLink);

        log.info("새 초대 코드 생성: travelPlanId={}, userId={}, code={}",
                travelPlanId, userId, savedLink.getInviteCode());

        return InviteLinkDto.builder()
                .code(savedLink.getInviteCode())
                .expiresAt(savedLink.getExpiresAt())
                .build();
    }

    /**
     * 코드 검증
     */
    public InviteLink validateInviteCode(String code) {
        if (code == null || code.length() != CODE_LENGTH) {
            throw new BadRequestException("유효하지 않은 초대 코드입니다.");
        }

        InviteLink inviteLink = inviteLinkRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("초대 코드", "code", code));

        if (!inviteLink.getIsActive()) {
            throw new BadRequestException("비활성화된 초대 코드입니다.");
        }

        if (inviteLink.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("만료된 초대 코드입니다.");
        }

        return inviteLink;
    }

    /**
     * 코드로 초대 수락
     */
    @Transactional
    public MemberDto acceptInviteByCode(String code, Long userId) {
        // 코드 검증
        InviteLink inviteLink = validateInviteCode(code);
        Long travelPlanId = inviteLink.getTravelPlan().getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "id", userId));

        // 이미 멤버인지 확인
        TravelPlanMember existingMember = memberRepository
                .findByTravelPlanIdAndUserId(travelPlanId, userId)
                .orElse(null);

        if (existingMember != null) {
            if (existingMember.getStatus() == InvitationStatus.ACCEPTED) {
                throw new BadRequestException("이미 이 여행 계획의 멤버입니다.");
            }
            // PENDING 또는 REJECTED 상태면 수락으로 변경
            existingMember.setStatus(InvitationStatus.ACCEPTED);
            existingMember.setJoinedAt(LocalDateTime.now());
            existingMember.setInvitedBy(inviteLink.getCreatedBy());

            // 기본 역할이 없으면 EDITOR로 설정
            if (existingMember.getRole() == null) {
                existingMember.setRole(MemberRole.EDITOR);
            }

            TravelPlanMember updatedMember = memberRepository.save(existingMember);
            log.info("초대 코드로 멤버십 업데이트: travelPlanId={}, userId={}", travelPlanId, userId);

            return memberMapper.toDto(updatedMember);
        }

        // 새 멤버 생성
        TravelPlanMember member = TravelPlanMember.builder()
                .travelPlan(inviteLink.getTravelPlan())
                .user(user)
                .role(MemberRole.EDITOR) // 기본 역할은 EDITOR
                .status(InvitationStatus.ACCEPTED)
                .invitedBy(inviteLink.getCreatedBy())
                .joinedAt(LocalDateTime.now())
                .build();

        TravelPlanMember savedMember = memberRepository.save(member);
        log.info("초대 코드로 멤버 추가: travelPlanId={}, userId={}, code={}",
                travelPlanId, userId, code);

        return memberMapper.toDto(savedMember);
    }

    /**
     * 초대 링크 무효화
     */
    @Transactional
    public void revokeInviteLink(Long travelPlanId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - OWNER 또는 EDITOR만 링크 무효화 가능
        if (!travelPlan.hasRole(userId, MemberRole.OWNER) &&
                !travelPlan.hasRole(userId, MemberRole.EDITOR)) {
            throw new ForbiddenException("초대 링크를 무효화할 권한이 없습니다. (소유자 또는 편집자만 가능)");
        }

        List<InviteLink> activeLinks = inviteLinkRepository.findByTravelPlanId(travelPlanId);
        activeLinks.forEach(link -> link.setIsActive(false));
        inviteLinkRepository.saveAll(activeLinks);

        log.info("초대 링크 무효화: travelPlanId={}, userId={}", travelPlanId, userId);
    }

    /**
     * 초대 코드 조회 (여행 계획의 활성화된 코드)
     */
    public InviteLinkDto getInviteCode(Long travelPlanId, Long userId) {
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획", "id", travelPlanId));

        // 권한 확인 - OWNER 또는 EDITOR만 코드 조회 가능
        if (!travelPlan.hasRole(userId, MemberRole.OWNER) &&
                !travelPlan.hasRole(userId, MemberRole.EDITOR)) {
            throw new ForbiddenException("초대 코드를 조회할 권한이 없습니다. (소유자 또는 편집자만 가능)");
        }

        // 활성화되고 유효한 코드 조회
        List<InviteLink> existingLinks = inviteLinkRepository.findByTravelPlanId(travelPlanId);
        LocalDateTime now = LocalDateTime.now();

        InviteLink activeLink = existingLinks.stream()
                .filter(link -> link.getIsActive() && link.getExpiresAt().isAfter(now))
                .findFirst()
                .orElse(null);

        if (activeLink == null) {
            throw new ResourceNotFoundException("초대 코드", "travelPlanId", travelPlanId);
        }

        return InviteLinkDto.builder()
                .code(activeLink.getInviteCode())
                .expiresAt(activeLink.getExpiresAt())
                .build();
    }

    /**
     * 초대 코드 정보로 여행 계획 정보 조회 (비인증)
     */
    public TravelPlanDto getInviteInfo(String code) {
        InviteLink inviteLink = validateInviteCode(code);
        TravelPlan travelPlan = inviteLink.getTravelPlan();

        // 제한된 정보만 반환 (멤버 정보는 제외)
        TravelPlanDto dto = travelPlanMapper.toDto(travelPlan);
        // 멤버 정보 제거 (비인증 조회이므로)
        if (dto != null) {
            dto.setMembers(null);
        }
        return dto;
    }
}
