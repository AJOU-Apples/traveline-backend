package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.ChatMessageDto;
import org.apples.travelinebackend.dto.ChatMessageResponse;
import org.apples.travelinebackend.dto.SendChatMessageRequest;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.ChatService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/travel-plans/{travelPlanId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 채팅 메시지 전송
     */
    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> sendMessage(
            @PathVariable Long travelPlanId,
            @Valid @RequestBody SendChatMessageRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/travel-plans/{}/chat/messages - 채팅 메시지 전송 요청", travelPlanId);
        ChatMessageDto message = chatService.sendMessage(travelPlanId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * 채팅 메시지 목록 조회 (페이징)
     */
    @GetMapping("/messages")
    public ResponseEntity<ChatMessageResponse> getMessages(
            @PathVariable Long travelPlanId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/chat/messages - 채팅 메시지 목록 조회, page={}, size={}", travelPlanId, page, size);
        ChatMessageResponse response = chatService.getMessages(travelPlanId, user, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 시점 이후 채팅 메시지 조회
     */
    @GetMapping("/messages/after")
    public ResponseEntity<List<ChatMessageDto>> getMessagesAfter(
            @PathVariable Long travelPlanId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/chat/messages/after - 특정 시점 이후 채팅 메시지 조회, after={}", travelPlanId, after);
        List<ChatMessageDto> messages = chatService.getMessagesAfter(travelPlanId, user, after);
        return ResponseEntity.ok(messages);
    }
}

