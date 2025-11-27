package org.apples.travelinebackend.config;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.security.JwtTokenProvider;
import org.apples.travelinebackend.websocket.TravelPlanWebSocketHandler;
import org.apples.travelinebackend.websocket.WebSocketJwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TravelPlanWebSocketHandler travelPlanWebSocketHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(travelPlanWebSocketHandler, "/ws/travel-plans/*")
                .addInterceptors(new org.apples.travelinebackend.websocket.WebSocketJwtInterceptor(jwtTokenProvider,
                        userDetailsService))
                .setAllowedOrigins("*"); // CORS 설정 - 필요시 특정 origin으로 제한
    }
}
