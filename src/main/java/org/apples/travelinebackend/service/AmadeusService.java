package org.apples.travelinebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.FlightSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AmadeusService {

    private final WebClient webClient;
    private final String apiKey;
    private final String apiSecret;

    private String cachedToken;
    private LocalDateTime tokenExpiry;

    // 공항 코드 매핑
    private static final Map<String, String> AIRPORT_NAMES = new HashMap<>();

    static {
        // 한국
        AIRPORT_NAMES.put("ICN", "인천국제공항");
        AIRPORT_NAMES.put("GMP", "김포국제공항");
        AIRPORT_NAMES.put("PUS", "김해국제공항");
        AIRPORT_NAMES.put("CJU", "제주국제공항");
        AIRPORT_NAMES.put("TAE", "대구국제공항");
        AIRPORT_NAMES.put("CJJ", "청주국제공항");
        
        // 일본
        AIRPORT_NAMES.put("NRT", "나리타국제공항");
        AIRPORT_NAMES.put("HND", "하네다공항");
        AIRPORT_NAMES.put("KIX", "간사이국제공항");
        AIRPORT_NAMES.put("ITM", "이타미공항");
        AIRPORT_NAMES.put("FUK", "후쿠오카공항");
        AIRPORT_NAMES.put("CTS", "신치토세공항");
        AIRPORT_NAMES.put("OKA", "나하공항");
        AIRPORT_NAMES.put("NGO", "주부 센트레아 국제공항");
        
        // 중국
        AIRPORT_NAMES.put("PEK", "베이징 수도 국제공항");
        AIRPORT_NAMES.put("PKX", "베이징 다싱 국제공항");
        AIRPORT_NAMES.put("PVG", "푸둥 국제공항");
        AIRPORT_NAMES.put("SHA", "훙차오 국제공항");
        AIRPORT_NAMES.put("HKG", "홍콩 국제공항");
        
        // 동남아
        AIRPORT_NAMES.put("BKK", "수완나품 국제공항");
        AIRPORT_NAMES.put("DMK", "돈므앙 국제공항");
        AIRPORT_NAMES.put("SIN", "창이 국제공항");
        AIRPORT_NAMES.put("HAN", "노이바이 국제공항");
        AIRPORT_NAMES.put("SGN", "탄손낫 국제공항");
        AIRPORT_NAMES.put("MNL", "니노이 아키노 국제공항");
        
        // 미국
        AIRPORT_NAMES.put("JFK", "존 F. 케네디 국제공항");
        AIRPORT_NAMES.put("LAX", "로스앤젤레스 국제공항");
        AIRPORT_NAMES.put("SFO", "샌프란시스코 국제공항");
        AIRPORT_NAMES.put("ORD", "시카고 오헤어 국제공항");
        AIRPORT_NAMES.put("ATL", "애틀랜타 국제공항");
        
        // 유럽
        AIRPORT_NAMES.put("LHR", "히드로 공항");
        AIRPORT_NAMES.put("CDG", "샤를 드골 공항");
        AIRPORT_NAMES.put("FRA", "프랑크푸르트 공항");
        AIRPORT_NAMES.put("AMS", "암스테르담 스키폴 공항");
        
        // 오세아니아
        AIRPORT_NAMES.put("SYD", "시드니 킹스포드 스미스 국제공항");
        AIRPORT_NAMES.put("MEL", "멜버른 공항");
    }

    public AmadeusService(
            WebClient.Builder webClientBuilder,
            @Value("${amadeus.api-url}") String apiUrl,
            @Value("${amadeus.api-key}") String apiKey,
            @Value("${amadeus.api-secret}") String apiSecret) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    /**
     * Amadeus API 액세스 토큰 획득
     */
    private Mono<String> getAccessToken() {
        // 캐시된 토큰이 유효한 경우 반환
        if (cachedToken != null && tokenExpiry != null && LocalDateTime.now().isBefore(tokenExpiry)) {
            log.debug("캐시된 Amadeus 토큰 사용");
            return Mono.just(cachedToken);
        }

        log.info("새로운 Amadeus 액세스 토큰 요청");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", apiKey);
        formData.add("client_secret", apiSecret);

        return webClient.post()
                .uri("/v1/security/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .map(response -> {
                    String token = response.get("access_token").asText();
                    int expiresIn = response.get("expires_in").asInt();
                    
                    // 토큰 캐싱 (만료 시간 - 60초 여유)
                    cachedToken = token;
                    tokenExpiry = LocalDateTime.now().plusSeconds(expiresIn - 60);
                    
                    log.info("Amadeus 액세스 토큰 획득 성공 (만료: {})", tokenExpiry);
                    return token;
                })
                .doOnError(error -> log.error("Amadeus 토큰 획득 실패: {}", error.getMessage()));
    }

    /**
     * 항공편 스케줄 정보 조회
     */
    public Mono<FlightSearchResponse> getFlightStatus(String carrierCode, String flightNumber, String scheduledDepartureDate) {
        log.info("항공편 정보 조회: {} {}, 출발일: {}", carrierCode, flightNumber, scheduledDepartureDate);

        return getAccessToken()
                .flatMap(token -> webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/v2/schedule/flights")
                                .queryParam("carrierCode", carrierCode)
                                .queryParam("flightNumber", flightNumber)
                                .queryParam("scheduledDepartureDate", scheduledDepartureDate)
                                .build())
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .bodyToMono(JsonNode.class)
                        .timeout(Duration.ofSeconds(10)))
                .map(response -> {
                    JsonNode data = response.get("data");
                    
                    if (data == null || !data.isArray() || data.size() == 0) {
                        log.warn("항공편 정보를 찾을 수 없음: {} {}", carrierCode, flightNumber);
                        return null;
                    }

                    JsonNode flight = data.get(0);
                    JsonNode flightPoints = flight.get("flightPoints");

                    if (flightPoints == null || flightPoints.size() < 2) {
                        log.warn("항공편 경로 정보 부족: {} {}", carrierCode, flightNumber);
                        return null;
                    }

                    // 출발지 정보
                    JsonNode departurePoint = flightPoints.get(0);
                    String departureCode = departurePoint.get("iataCode").asText();
                    JsonNode departureTimings = departurePoint.get("departure").get("timings");
                    String departureTime = extractScheduledTime(departureTimings, "STD");

                    // 도착지 정보
                    JsonNode arrivalPoint = flightPoints.get(flightPoints.size() - 1);
                    String arrivalCode = arrivalPoint.get("iataCode").asText();
                    JsonNode arrivalTimings = arrivalPoint.get("arrival").get("timings");
                    String arrivalTime = extractScheduledTime(arrivalTimings, "STA");

                    if (departureTime == null || arrivalTime == null) {
                        log.warn("출발/도착 시간 정보 없음: {} {}", carrierCode, flightNumber);
                        return null;
                    }

                    FlightSearchResponse flightInfo = FlightSearchResponse.builder()
                            .airline(carrierCode)
                            .flightNumber(flightNumber)
                            .departureAirport(getAirportName(departureCode))
                            .departureAirportCode(departureCode)
                            .arrivalAirport(getAirportName(arrivalCode))
                            .arrivalAirportCode(arrivalCode)
                            .departureTime(departureTime)
                            .arrivalTime(arrivalTime)
                            .scheduledDepartureDate(scheduledDepartureDate)
                            .build();

                    log.info("항공편 정보 조회 성공: {} {} → {}", departureCode, arrivalCode, flightInfo);
                    return flightInfo;
                })
                .onErrorResume(error -> {
                    log.error("항공편 정보 조회 중 오류: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Scheduled 시간 추출 및 HH:MM 형식으로 변환
     */
    private String extractScheduledTime(JsonNode timings, String qualifier) {
        if (timings == null || !timings.isArray()) {
            return null;
        }

        for (JsonNode timing : timings) {
            if (qualifier.equals(timing.get("qualifier").asText())) {
                String isoTime = timing.get("value").asText();
                return formatTime(isoTime);
            }
        }
        return null;
    }

    /**
     * ISO 8601 시간 형식을 HH:MM으로 변환
     */
    private String formatTime(String isoString) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME);
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            log.warn("시간 형식 변환 실패: {}", isoString);
            return null;
        }
    }

    /**
     * 공항 코드를 공항 이름으로 변환
     */
    private String getAirportName(String airportCode) {
        return AIRPORT_NAMES.getOrDefault(airportCode, airportCode);
    }
}

