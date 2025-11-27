package org.apples.travelinebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketChatMessageRequest {
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("data")
    private ChatMessageData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessageData {
        private String message;
    }
}

