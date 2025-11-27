package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.websocket.TravelPlanWebSocketHandler;
import org.springframework.stereotype.Service;

/**
 * WebSocket을 통한 이벤트 브로드캐스트 서비스
 * REST API에서 변경이 발생하면 해당 여행 계획의 모든 멤버에게 이벤트를 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketEventService {

    private final TravelPlanWebSocketHandler webSocketHandler;

    // ==================== Place Events ====================

    public void broadcastPlaceAdded(Long travelPlanId, PlaceDto placeDto) {
        TravelPlanEvent.PlaceAddedEvent event = new TravelPlanEvent.PlaceAddedEvent(placeDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastPlaceUpdated(Long travelPlanId, PlaceDto placeDto) {
        TravelPlanEvent.PlaceUpdatedEvent event = new TravelPlanEvent.PlaceUpdatedEvent(placeDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastPlaceDeleted(Long travelPlanId, Long placeId) {
        TravelPlanEvent.PlaceDeletedEvent event = new TravelPlanEvent.PlaceDeletedEvent(placeId);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastPlaceReordered(Long travelPlanId, Integer dayNumber, java.util.List<Long> placeIds) {
        TravelPlanEvent.PlaceReorderedEvent event = new TravelPlanEvent.PlaceReorderedEvent(dayNumber, placeIds);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Expense Events ====================

    public void broadcastExpenseAdded(Long travelPlanId, ExpenseDto expenseDto) {
        TravelPlanEvent.ExpenseAddedEvent event = new TravelPlanEvent.ExpenseAddedEvent(expenseDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastExpenseUpdated(Long travelPlanId, ExpenseDto expenseDto) {
        TravelPlanEvent.ExpenseUpdatedEvent event = new TravelPlanEvent.ExpenseUpdatedEvent(expenseDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastExpenseDeleted(Long travelPlanId, Long expenseId) {
        TravelPlanEvent.ExpenseDeletedEvent event = new TravelPlanEvent.ExpenseDeletedEvent(expenseId);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Photo Events ====================

    public void broadcastPhotoAdded(Long travelPlanId, PhotoDto photoDto) {
        TravelPlanEvent.PhotoAddedEvent event = new TravelPlanEvent.PhotoAddedEvent(photoDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Flight Events ====================

    public void broadcastFlightUpdated(Long travelPlanId, FlightDto flightDto) {
        TravelPlanEvent.FlightUpdatedEvent event = new TravelPlanEvent.FlightUpdatedEvent(flightDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Accommodation Events ====================

    public void broadcastAccommodationUpdated(Long travelPlanId, AccommodationDto accommodationDto) {
        TravelPlanEvent.AccommodationUpdatedEvent event = new TravelPlanEvent.AccommodationUpdatedEvent(
                accommodationDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Memo Events ====================

    public void broadcastMemoAdded(Long travelPlanId, MemoDto memoDto) {
        TravelPlanEvent.MemoAddedEvent event = new TravelPlanEvent.MemoAddedEvent(memoDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastMemoUpdated(Long travelPlanId, MemoDto memoDto) {
        TravelPlanEvent.MemoUpdatedEvent event = new TravelPlanEvent.MemoUpdatedEvent(memoDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastMemoDeleted(Long travelPlanId, Long memoId) {
        TravelPlanEvent.MemoDeletedEvent event = new TravelPlanEvent.MemoDeletedEvent(memoId);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    // ==================== Member Events ====================

    public void broadcastMemberJoined(Long travelPlanId, MemberDto memberDto) {
        TravelPlanEvent.MemberJoinedEvent event = new TravelPlanEvent.MemberJoinedEvent(memberDto);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }

    public void broadcastMemberLeft(Long travelPlanId, Long memberId) {
        TravelPlanEvent.MemberLeftEvent event = new TravelPlanEvent.MemberLeftEvent(memberId);
        webSocketHandler.broadcastToPlan(travelPlanId, event);
    }
}
