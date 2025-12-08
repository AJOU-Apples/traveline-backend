package org.apples.travelinebackend.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.security.JwtTokenProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 연결 시 JWT 토큰을 검증하는 인터셉터
 * 쿼리 파라미터 또는 헤더에서 토큰을 추출하여 검증
 */
@Slf4j
public class WebSocketJwtInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    public WebSocketJwtInterceptor(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            // 1. 쿼리 파라미터에서 토큰 추출 시도
            String token = extractTokenFromQuery(request.getURI());

            // 2. 쿼리 파라미터에 없으면 헤더에서 추출 시도
            if (!StringUtils.hasText(token)) {
                token = extractTokenFromHeader(request);
            }

            // 3. 토큰 검증
            if (!StringUtils.hasText(token) || !jwtTokenProvider.validateToken(token)) {
                log.warn("WebSocket 연결 실패: 유효하지 않은 토큰");
                response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                return false;
            }

            // 4. 사용자 정보 추출 및 저장
            String email = jwtTokenProvider.getEmailFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // attributes에 사용자 정보 저장 (핸들러에서 사용)
            attributes.put("userEmail", email);
            attributes.put("userId", extractUserId(userDetails));

            log.info("WebSocket 연결 성공: email={}", email);
            return true;

        } catch (Exception e) {
            log.error("WebSocket 연결 중 오류 발생", e);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 완료 후 처리 (필요시)
    }

    /**
     * 쿼리 파라미터에서 토큰 추출
     * 예: ws://localhost:8080/ws/travel-plans/1?token=xxx
     */
    private String extractTokenFromQuery(URI uri) {
        String query = uri.getQuery();
        if (query == null) {
            return null;
        }

        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    /**
     * 헤더에서 토큰 추출
     * Authorization: Bearer xxx 형식 지원
     */
    private String extractTokenFromHeader(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * UserDetails에서 userId 추출
     * User 엔티티의 Principal을 통해 userId를 가져옴
     */
    private Long extractUserId(UserDetails userDetails) {
        if (userDetails instanceof org.apples.travelinebackend.entity.User) {
            return ((org.apples.travelinebackend.entity.User) userDetails).getId();
        }
        // Principal이 User 객체가 아닌 경우를 대비한 처리
        return null;
    }
}
