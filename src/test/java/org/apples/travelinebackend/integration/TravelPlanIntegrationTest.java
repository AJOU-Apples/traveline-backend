package org.apples.travelinebackend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.repository.CityRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {
                "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("TravelPlan 통합 테스트")
class TravelPlanIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private TravelPlanRepository travelPlanRepository;

        @Autowired
        private CityRepository CityRepository;

        @BeforeEach
        void setUp() {
                travelPlanRepository.deleteAll();
                CityRepository.deleteAll();
        }

        @Test
        @DisplayName("여행 계획 생성 후 조회")
        void createAndRetrieveTravelPlan() throws Exception {
                // given
                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .latitude(35.6762)
                                .longitude(139.6503)
                                .build();

                CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                // when - 생성
                String createResponse = mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                                .andExpect(jsonPath("$.destination.name").value("도쿄"))
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

                // then - 조회
                mockMvc.perform(get("/api/travel-plans/{planId}", createdId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(createdId))
                                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                                .andExpect(jsonPath("$.destination.name").value("도쿄"));
        }

        @Test
        @DisplayName("여행 계획 생성 시 일차 정보 포함")
        void createTravelPlanWithDays() throws Exception {
                // given
                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                TravelDayDto day1 = TravelDayDto.builder()
                                .dayNumber(1)
                                .date("2024-11-20")
                                .displayDate("11월 20일(수)")
                                .build();

                TravelDayDto day2 = TravelDayDto.builder()
                                .dayNumber(2)
                                .date("2024-11-21")
                                .displayDate("11월 21일(목)")
                                .build();

                CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(List.of(day1, day2))
                                .build();

                // when & then
                mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.days", hasSize(2)))
                                .andExpect(jsonPath("$.days[0].dayNumber").value(1))
                                .andExpect(jsonPath("$.days[1].dayNumber").value(2));
        }

        @Test
        @DisplayName("여행 계획 수정")
        void updateTravelPlan() throws Exception {
                // given - 여행 계획 생성
                CityDto originalDestination = CityDto.builder()
                                .name("도쿄")
                                .build();

                CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(originalDestination)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                String createResponse = mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long planId = objectMapper.readTree(createResponse).get("id").asLong();

                // when - 수정
                CityDto newDestination = CityDto.builder()
                                .name("오사카")
                                .build();

                UpdateTravelPlanRequest updateRequest = UpdateTravelPlanRequest.builder()
                                .title("오사카 여행")
                                .destination(newDestination)
                                .participants(3)
                                .build();

                // then
                mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("오사카 여행"))
                                .andExpect(jsonPath("$.destination.name").value("오사카"))
                                .andExpect(jsonPath("$.participants").value(3));
        }

        @Test
        @DisplayName("여행 계획 삭제")
        void deleteTravelPlan() throws Exception {
                // given - 여행 계획 생성
                CityDto destination = CityDto.builder()
                                .name("도쿄")
                                .build();

                CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(destination)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                String createResponse = mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long planId = objectMapper.readTree(createResponse).get("id").asLong();

                // when - 삭제
                mockMvc.perform(delete("/api/travel-plans/{planId}", planId))
                                .andExpect(status().isNoContent());

                // then - 조회 시 없음 확인
                mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("여행 계획 아카이브")
        void archiveTravelPlan() throws Exception {
                // given - 여행 계획 생성
                CityDto destination = CityDto.builder()
                                .name("도쿄")
                                .build();

                CreateTravelPlanRequest createRequest = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(destination)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                String createResponse = mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest)))
                                .andExpect(status().isCreated())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                Long planId = objectMapper.readTree(createResponse).get("id").asLong();

                // when - 아카이브
                mockMvc.perform(post("/api/travel-plans/{planId}/archive", planId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.isArchived").value(true));

                // then - 일반 목록에서는 조회되지 않음
                mockMvc.perform(get("/api/travel-plans")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        @DisplayName("페이징된 여행 계획 목록 조회")
        void getTravelPlansWithPaging() throws Exception {
                // given - 여러 여행 계획 생성
                for (int i = 1; i <= 3; i++) {
                        CityDto destination = CityDto.builder()
                                        .name("도시" + i)
                                        .build();

                        CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                        .title("여행 " + i)
                                        .destination(destination)
                                        .startDate("2024.11.20")
                                        .endDate("2024.11.23")
                                        .participants(2)
                                        .days(new ArrayList<>())
                                        .build();

                        mockMvc.perform(post("/api/travel-plans")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());
                }

                // when & then
                mockMvc.perform(get("/api/travel-plans")
                                .param("page", "0")
                                .param("limit", "2"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)))
                                .andExpect(jsonPath("$.totalElements").value(3))
                                .andExpect(jsonPath("$.totalPages").value(2));
        }
}
