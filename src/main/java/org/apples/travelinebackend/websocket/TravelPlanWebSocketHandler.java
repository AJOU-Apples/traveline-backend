package org.apples.travelinebackend.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.TravelPlanEvent;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 여행 계획별 WebSocket 연결을 관리하는 핸들러
 * 각 여행 계획(planId)별로 연결된 세션들을 그룹화하여 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TravelPlanWebSocketHandler extends TextWebSocketHandler {

    private final TravelPlanRepository travelPlanRepository;
    private final ObjectMapper objectMapper;
    
    // planId -> Set<WebSocketSession> 매핑
    private final Map<Long, Map<String, WebSocketSession>> planSessions = new ConcurrentHashMap<>();
    
    // sessionId -> planId 매핑 (연결 해제 시 사용)
    private final Map<String, Long> sessionToPlanId = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long planId = extractPlanId(session);
        Long userId = extractUserId(session);
        
        if (planId == null) {
            log.warn("WebSocket 연결 실패: planId를 찾을 수 없음");
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        
        // 여행 계획 존재 여부 확인
        if (!travelPlanRepository.existsById(planId)) {
            log.warn("WebSocket 연결 실패: 존재하지 않는 여행 계획 - planId={}", planId);
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        
        // 세션 등록
        planSessions.computeIfAbsent(planId, k -> new ConcurrentHashMap<>())
                .put(session.getId(), session);
        sessionToPlanId.put(session.getId(), planId);
        
        log.info("WebSocket 연결 성공: planId={}, userId={}, sessionId={}", planId, userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long planId = sessionToPlanId.remove(session.getId());
        
        if (planId != null) {
            Map<String, WebSocketSession> sessions = planSessions.get(planId);
            if (sessions != null) {
                sessions.remove(session.getId());
                
                // 세션이 없으면 planId도 제거
                if (sessions.isEmpty()) {
                    planSessions.remove(planId);
                }
            }
        }
        
        log.info("WebSocket 연결 종료: planId={}, sessionId={}, status={}", planId, session.getId(), status);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지를 받는 경우 (필요시 구현)
        log.debug("WebSocket 메시지 수신: sessionId={}, message={}", session.getId(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 전송 오류: sessionId={}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * 특정 여행 계획의 모든 연결된 세션에 이벤트 브로드캐스트
     */
    public void broadcastToPlan(Long planId, TravelPlanEvent event) {
        Map<String, WebSocketSession> sessions = planSessions.get(planId);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("브로드캐스트 대상 없음: planId={}", planId);
            return;
        }

        try {
            String message = objectMapper.writeValueAsString(event);
            TextMessage textMessage = new TextMessage(message);
            
            int successCount = 0;
            int failCount = 0;
            
            for (WebSocketSession session : sessions.values()) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        successCount++;
                    } else {
                        // 닫힌 세션은 제거
                        sessions.remove(session.getId());
                        sessionToPlanId.remove(session.getId());
                        failCount++;
                    }
                } catch (IOException e) {
                    log.error("WebSocket 메시지 전송 실패: sessionId={}, planId={}", session.getId(), planId, e);
                    failCount++;
                    // 오류 발생한 세션 제거
                    sessions.remove(session.getId());
                    sessionToPlanId.remove(session.getId());
                }
            }
            
            log.info("이벤트 브로드캐스트 완료: planId={}, type={}, 성공={}, 실패={}", 
                    planId, event.getType(), successCount, failCount);
                    
        } catch (Exception e) {
            log.error("이벤트 브로드캐스트 중 오류 발생: planId={}", planId, e);
        }
    }

    /**
     * URI에서 planId 추출
     * 예: /ws/travel-plans/123 -> 123
     */
    private Long extractPlanId(WebSocketSession session) {
        String uri = session.getUri().getPath();
        String[] parts = uri.split("/");
        
        for (int i = 0; i < parts.length; i++) {
            if ("travel-plans".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Long.parseLong(parts[i + 1]);
                } catch (NumberFormatException e) {
                    log.error("잘못된 planId 형식: {}", parts[i + 1]);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 세션 attributes에서 userId 추출
     */
    private Long extractUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }
}

