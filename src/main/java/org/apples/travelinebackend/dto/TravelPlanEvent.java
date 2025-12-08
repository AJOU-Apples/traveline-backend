package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * WebSocket을 통해 전송되는 여행 계획 이벤트
 * Jackson의 다형성 지원을 사용하여 타입별로 다른 data 구조를 가짐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
        // Place 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.PlaceAddedEvent.class, name = "PLACE_ADDED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.PlaceUpdatedEvent.class, name = "PLACE_UPDATED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.PlaceDeletedEvent.class, name = "PLACE_DELETED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.PlaceReorderedEvent.class, name = "PLACE_REORDERED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.PlaceLikeChangedEvent.class, name = "PLACE_LIKE_CHANGED"),

        // Expense 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.ExpenseAddedEvent.class, name = "EXPENSE_ADDED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.ExpenseUpdatedEvent.class, name = "EXPENSE_UPDATED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.ExpenseDeletedEvent.class, name = "EXPENSE_DELETED"),

        // Photo 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.PhotoAddedEvent.class, name = "PHOTO_ADDED"),

        // Flight 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.FlightUpdatedEvent.class, name = "FLIGHT_UPDATED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.FlightLikeChangedEvent.class, name = "FLIGHT_LIKE_CHANGED"),

        // Accommodation 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.AccommodationUpdatedEvent.class, name = "ACCOMMODATION_UPDATED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.AccommodationLikeChangedEvent.class, name = "ACCOMMODATION_LIKE_CHANGED"),

        // Memo 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.MemoAddedEvent.class, name = "MEMO_ADDED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.MemoUpdatedEvent.class, name = "MEMO_UPDATED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.MemoDeletedEvent.class, name = "MEMO_DELETED"),

        // Member 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.MemberJoinedEvent.class, name = "MEMBER_JOINED"),
        @JsonSubTypes.Type(value = TravelPlanEvent.MemberLeftEvent.class, name = "MEMBER_LEFT"),

        // Chat 이벤트
        @JsonSubTypes.Type(value = TravelPlanEvent.ChatMessageSentEvent.class, name = "CHAT_MESSAGE_SENT")
})
public abstract class TravelPlanEvent {
    private String type;

    // ==================== Place Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PlaceAddedEvent extends TravelPlanEvent {
        private PlaceDto data;

        public PlaceAddedEvent(PlaceDto data) {
            super("PLACE_ADDED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PlaceUpdatedEvent extends TravelPlanEvent {
        private PlaceDto data;

        public PlaceUpdatedEvent(PlaceDto data) {
            super("PLACE_UPDATED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PlaceDeletedEvent extends TravelPlanEvent {
        private PlaceDeletedData data;

        public PlaceDeletedEvent(Long placeId) {
            super("PLACE_DELETED");
            this.data = new PlaceDeletedData(placeId);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PlaceReorderedEvent extends TravelPlanEvent {
        private PlaceReorderedData data;

        public PlaceReorderedEvent(Integer dayNumber, java.util.List<Long> placeIds) {
            super("PLACE_REORDERED");
            this.data = new PlaceReorderedData(dayNumber, placeIds);
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PlaceLikeChangedEvent extends TravelPlanEvent {
        private PlaceLikeChangedData data;

        public PlaceLikeChangedEvent(Long placeId, Long userId, boolean isLiked, long likeCount) {
            super("PLACE_LIKE_CHANGED");
            this.data = new PlaceLikeChangedData(placeId, userId, isLiked, likeCount);
        }
    }

    // ==================== Expense Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class ExpenseAddedEvent extends TravelPlanEvent {
        private ExpenseDto data;

        public ExpenseAddedEvent(ExpenseDto data) {
            super("EXPENSE_ADDED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class ExpenseUpdatedEvent extends TravelPlanEvent {
        private ExpenseDto data;

        public ExpenseUpdatedEvent(ExpenseDto data) {
            super("EXPENSE_UPDATED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class ExpenseDeletedEvent extends TravelPlanEvent {
        private ExpenseDeletedData data;

        public ExpenseDeletedEvent(Long expenseId) {
            super("EXPENSE_DELETED");
            this.data = new ExpenseDeletedData(expenseId);
        }
    }

    // ==================== Photo Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PhotoAddedEvent extends TravelPlanEvent {
        private PhotoDto data;

        public PhotoAddedEvent(PhotoDto data) {
            super("PHOTO_ADDED");
            this.data = data;
        }
    }

    // ==================== Flight Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class FlightUpdatedEvent extends TravelPlanEvent {
        private FlightDto data;

        public FlightUpdatedEvent(FlightDto data) {
            super("FLIGHT_UPDATED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class FlightLikeChangedEvent extends TravelPlanEvent {
        private FlightLikeChangedData data;

        public FlightLikeChangedEvent(Long flightId, Long userId, boolean isLiked, long likeCount) {
            super("FLIGHT_LIKE_CHANGED");
            this.data = new FlightLikeChangedData(flightId, userId, isLiked, likeCount);
        }
    }

    // ==================== Accommodation Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class AccommodationUpdatedEvent extends TravelPlanEvent {
        private AccommodationDto data;

        public AccommodationUpdatedEvent(AccommodationDto data) {
            super("ACCOMMODATION_UPDATED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class AccommodationLikeChangedEvent extends TravelPlanEvent {
        private AccommodationLikeChangedData data;

        public AccommodationLikeChangedEvent(Long accommodationId, Long userId, boolean isLiked, long likeCount) {
            super("ACCOMMODATION_LIKE_CHANGED");
            this.data = new AccommodationLikeChangedData(accommodationId, userId, isLiked, likeCount);
        }
    }

    // ==================== Memo Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class MemoAddedEvent extends TravelPlanEvent {
        private MemoDto data;

        public MemoAddedEvent(MemoDto data) {
            super("MEMO_ADDED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class MemoUpdatedEvent extends TravelPlanEvent {
        private MemoDto data;

        public MemoUpdatedEvent(MemoDto data) {
            super("MEMO_UPDATED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class MemoDeletedEvent extends TravelPlanEvent {
        private MemoDeletedData data;

        public MemoDeletedEvent(Long memoId) {
            super("MEMO_DELETED");
            this.data = new MemoDeletedData(memoId);
        }
    }

    // ==================== Member Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class MemberJoinedEvent extends TravelPlanEvent {
        private MemberDto data;

        public MemberJoinedEvent(MemberDto data) {
            super("MEMBER_JOINED");
            this.data = data;
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class MemberLeftEvent extends TravelPlanEvent {
        private MemberLeftData data;

        public MemberLeftEvent(Long memberId) {
            super("MEMBER_LEFT");
            this.data = new MemberLeftData(memberId);
        }
    }

    // ==================== Data Classes ====================

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceDeletedData {
        private Long placeId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceReorderedData {
        private Integer dayNumber;
        private java.util.List<Long> placeIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseDeletedData {
        private Long expenseId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberLeftData {
        private Long memberId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemoDeletedData {
        private Long memoId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceLikeChangedData {
        private Long placeId;
        private Long userId;
        private boolean isLiked;
        private long likeCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightLikeChangedData {
        private Long flightId;
        private Long userId;
        private boolean isLiked;
        private long likeCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccommodationLikeChangedData {
        private Long accommodationId;
        private Long userId;
        private boolean isLiked;
        private long likeCount;
    }

    // ==================== Chat Events ====================

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class ChatMessageSentEvent extends TravelPlanEvent {
        private ChatMessageDto data;

        public ChatMessageSentEvent(ChatMessageDto data) {
            super("CHAT_MESSAGE_SENT");
            this.data = data;
        }
    }
}
