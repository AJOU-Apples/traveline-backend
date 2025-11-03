package org.apples.travelinebackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("TravelPlan API 통합 테스트")
class TravelPlanIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TravelPlanRepository travelPlanRepository;

    @AfterEach
    void tearDown() {
        travelPlanRepository.deleteAll();
    }

    @Test
    @DisplayName("전체 시나리오 테스트: 생성 -> 조회 -> 수정 -> 삭제")
    void fullScenario_CreateReadUpdateDelete() throws Exception {
        // 1. 여행 계획 생성
        TravelDayDto dayDto = TravelDayDto.builder()
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(new ArrayList<>())
                .build();

        CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(dayDto))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long planId = objectMapper.readTree(responseBody).get("id").asLong();

        // 2. 생성된 여행 계획 조회
        mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planId))
                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                .andExpect(jsonPath("$.days", hasSize(1)));

        // 3. 모든 여행 계획 조회 (페이징)
        mockMvc.perform(get("/api/travel-plans"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        // 4. 여행 계획 수정
        UpdateTravelPlanRequest updateRequest = UpdateTravelPlanRequest.builder()
                .title("도쿄 여행 (수정됨)")
                .participants(3)
                .build();

        mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("도쿄 여행 (수정됨)"))
                .andExpect(jsonPath("$.participants").value(3));

        // 5. 여행 계획 삭제
        mockMvc.perform(delete("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 6. 삭제된 계획 조회 시 404
        mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("장소 정보 포함 여행 계획 전체 시나리오")
    void fullScenario_WithPlaces() throws Exception {
        // 1. 장소 정보 포함 여행 계획 생성
        PlaceDto place1 = PlaceDto.builder()
                .name("도쿄 타워")
                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                .time("14:00")
                .latitude(35.6585805)
                .longitude(139.7454329)
                .build();

        PlaceDto place2 = PlaceDto.builder()
                .name("아사쿠사")
                .address("Asakusa, Taito City, Tokyo")
                .time("17:00")
                .latitude(35.7148)
                .longitude(139.7967)
                .build();

        TravelDayDto dayDto = TravelDayDto.builder()
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(List.of(place1, place2))
                .build();

        CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(dayDto))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.days[0].places", hasSize(2)))
                .andExpect(jsonPath("$.days[0].places[0].name").value("도쿄 타워"))
                .andExpect(jsonPath("$.days[0].places[1].name").value("아사쿠사"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long planId = objectMapper.readTree(responseBody).get("id").asLong();

        // 2. 장소 정보 조회 확인
        mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[0].places", hasSize(2)))
                .andExpect(jsonPath("$.days[0].places[0].latitude").value(35.6585805))
                .andExpect(jsonPath("$.days[0].places[0].longitude").value(139.7454329));
    }

    @Test
    @DisplayName("여러 여행 계획 생성 후 목록 조회")
    void createMultiplePlans_AndList() throws Exception {
        // given - 3개의 여행 계획 생성
        CreateTravelPlanRequest plan1 = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(new ArrayList<>())
                .build();

        CreateTravelPlanRequest plan2 = CreateTravelPlanRequest.builder()
                .title("파리 여행")
                .destination("파리")
                .startDate("2024.12.01")
                .endDate("2024.12.05")
                .participants(3)
                .days(new ArrayList<>())
                .build();

        CreateTravelPlanRequest plan3 = CreateTravelPlanRequest.builder()
                .title("뉴욕 여행")
                .destination("뉴욕")
                .startDate("2025.01.10")
                .endDate("2025.01.15")
                .participants(4)
                .days(new ArrayList<>())
                .build();

        mockMvc.perform(post("/api/travel-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan1)));

        mockMvc.perform(post("/api/travel-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan2)));

        mockMvc.perform(post("/api/travel-plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(plan3)));

        // when & then - 모든 계획 조회 (페이징)
        mockMvc.perform(get("/api/travel-plans"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[1].title").exists())
                .andExpect(jsonPath("$.content[2].title").exists());
    }

    @Test
    @DisplayName("일차 데이터 업데이트 테스트")
    void updatePlan_ReplaceDays() throws Exception {
        // 1. 초기 여행 계획 생성 (1일차만)
        TravelDayDto day1 = TravelDayDto.builder()
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(new ArrayList<>())
                .build();

        CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(day1))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long planId = objectMapper.readTree(responseBody).get("id").asLong();

        // 2. 일차 데이터 업데이트 (2일차 추가)
        TravelDayDto updatedDay1 = TravelDayDto.builder()
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(new ArrayList<>())
                .build();

        TravelDayDto day2 = TravelDayDto.builder()
                .dayNumber(2)
                .date("2024-11-21")
                .displayDate("11월 21일(목)")
                .places(new ArrayList<>())
                .build();

        UpdateTravelPlanRequest updateRequest = UpdateTravelPlanRequest.builder()
                .days(List.of(updatedDay1, day2))
                .build();

        mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days", hasSize(2)));
    }

    @Test
    @DisplayName("유효성 검증 실패 테스트")
    void validation_Failure() throws Exception {
        // 1. 필수 필드 누락
        CreateTravelPlanRequest invalidRequest1 = CreateTravelPlanRequest.builder()
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                // title 누락
                .participants(2)
                .build();

        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest1)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 검증 실패"));

        // 2. 참가자 수 0 이하
        CreateTravelPlanRequest invalidRequest2 = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(0)
                .build();

        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest2)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

