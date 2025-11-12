package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.DirectionsApiResponse;
import org.apples.travelinebackend.dto.PlaceSearchResult;
import org.apples.travelinebackend.dto.PlacesApiResponse;
import org.apples.travelinebackend.dto.RouteInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    @Value("${google.maps.api-key}")
    private String apiKey;

    private final WebClient webClient;

    /**
     * Google Places API - Text Search
     * 장소 이름으로 검색
     */
    public List<PlaceSearchResult> searchPlaces(String query, Double lat, Double lng) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/textsearch/json");
            urlBuilder.append("?query=").append(encodedQuery);
            urlBuilder.append("&key=").append(apiKey);
            urlBuilder.append("&language=ko");  // 한국어 결과

            // 위치 기반 검색 (optional)
            if (lat != null && lng != null) {
                urlBuilder.append("&location=").append(lat).append(",").append(lng);
                urlBuilder.append("&radius=5000");  // 5km 반경
            }

            String url = urlBuilder.toString();
            log.info("Google Places API 호출: query={}, lat={}, lng={}", query, lat, lng);

            PlacesApiResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(PlacesApiResponse.class)
                    .block();

            if (response != null && "OK".equals(response.getStatus())) {
                log.info("장소 검색 성공: {} 건", response.getResults().size());
                return response.getResults();
            } else {
                log.warn("Google Places API 응답 실패: status={}", response != null ? response.getStatus() : "null");
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Google Places API 호출 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Google Directions API
     * 두 지점 간의 경로 정보 조회
     */
    public RouteInfo getRoute(String origin, String destination) {
        try {
            StringBuilder urlBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json");
            urlBuilder.append("?origin=").append(URLEncoder.encode(origin, StandardCharsets.UTF_8));
            urlBuilder.append("&destination=").append(URLEncoder.encode(destination, StandardCharsets.UTF_8));
            urlBuilder.append("&key=").append(apiKey);
            urlBuilder.append("&language=ko");
            urlBuilder.append("&mode=transit");  // 대중교통 기준

            String url = urlBuilder.toString();
            log.info("Google Directions API 호출: origin={}, destination={}", origin, destination);

            DirectionsApiResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(DirectionsApiResponse.class)
                    .block();

            if (response != null && "OK".equals(response.getStatus())) {
                RouteInfo routeInfo = response.toRouteInfo();
                log.info("경로 조회 성공: distance={}m, duration={}s", 
                        routeInfo.getDistance(), routeInfo.getDuration());
                return routeInfo;
            } else {
                log.warn("Google Directions API 응답 실패: status={}", 
                        response != null ? response.getStatus() : "null");
                return null;
            }
        } catch (Exception e) {
            log.error("Google Directions API 호출 실패: {}", e.getMessage(), e);
            return null;
        }
    }
}


