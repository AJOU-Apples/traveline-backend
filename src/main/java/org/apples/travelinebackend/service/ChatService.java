package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.ChatMessageDto;
import org.apples.travelinebackend.dto.ChatMessageResponse;
import org.apples.travelinebackend.dto.SendChatMessageRequest;
import org.apples.travelinebackend.entity.ChatMessage;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.ChatMessageMapper;
import org.apples.travelinebackend.repository.ChatMessageRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final ChatMessageMapper chatMessageMapper;
    
    @Lazy
    @Autowired
    private WebSocketEventService webSocketEventService;

    /**
     * 채팅 메시지 전송
     * 메시지를 저장하고 WebSocket을 통해 브로드캐스트
     */
    @Transactional
    public ChatMessageDto sendMessage(Long travelPlanId, User user, SendChatMessageRequest request) {
        log.info("채팅 메시지 전송 요청: travelPlanId={}, userId={}", travelPlanId, user.getId());

        // TravelPlan 조회 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        // 여행 계획 멤버 확인 (ACCEPTED 상태의 멤버만 채팅 가능)
        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("채팅 메시지를 전송할 권한이 없습니다");
        }

        // ChatMessage 생성 및 저장
        ChatMessage chatMessage = ChatMessage.builder()
                .travelPlan(travelPlan)
                .user(user)
                .message(request.getMessage())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("채팅 메시지 저장 완료: messageId={}", savedMessage.getId());

        // DTO 변환
        ChatMessageDto messageDto = chatMessageMapper.toDto(savedMessage);

        // WebSocket을 통해 브로드캐스트
        webSocketEventService.broadcastChatMessage(travelPlanId, messageDto);

        return messageDto;
    }

    /**
     * 채팅 메시지 목록 조회 (페이징)
     * 최신 메시지가 먼저 오도록 정렬 (내림차순)
     */
    public ChatMessageResponse getMessages(Long travelPlanId, User user, int page, int size) {
        log.info("채팅 메시지 목록 조회: travelPlanId={}, userId={}, page={}, size={}", travelPlanId, user.getId(), page, size);

        // TravelPlan 조회 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        // 여행 계획 멤버 확인
        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("채팅 메시지를 조회할 권한이 없습니다");
        }

        // 페이징 설정 (최신순 정렬)
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessage> messagePage = chatMessageRepository.findByTravelPlanIdOrderByCreatedAtDesc(travelPlanId, pageable);

        // DTO 변환
        List<ChatMessageDto> messageDtos = messagePage.getContent().stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());

        return ChatMessageResponse.builder()
                .messages(messageDtos)
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .currentPage(messagePage.getNumber())
                .hasNext(messagePage.hasNext())
                .hasPrevious(messagePage.hasPrevious())
                .build();
    }

    /**
     * 특정 시점 이후의 채팅 메시지 조회
     * 주로 실시간 업데이트를 위해 사용
     */
    public List<ChatMessageDto> getMessagesAfter(Long travelPlanId, User user, LocalDateTime after) {
        log.info("특정 시점 이후 채팅 메시지 조회: travelPlanId={}, userId={}, after={}", travelPlanId, user.getId(), after);

        // TravelPlan 조회 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        // 여행 계획 멤버 확인
        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("채팅 메시지를 조회할 권한이 없습니다");
        }

        // 특정 시점 이후 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByTravelPlanIdAndCreatedAtAfter(travelPlanId, after);

        // DTO 변환
        return messages.stream()
                .map(chatMessageMapper::toDto)
                .collect(Collectors.toList());
    }
}

