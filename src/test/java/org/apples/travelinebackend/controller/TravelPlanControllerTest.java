package org.apples.travelinebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.service.TravelPlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelPlanController.class)
@DisplayName("TravelPlanController 단위 테스트")
class TravelPlanControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private TravelPlanService travelPlanService;

        @Test
        @DisplayName("여행 계획 생성 API 성공")
        void createTravelPlan_Success() throws Exception {
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

                TravelPlanDto responseDto = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .isArchived(false)
                                .days(new ArrayList<>())
                                .build();

                when(travelPlanService.createTravelPlan(any(CreateTravelPlanRequest.class)))
                                .thenReturn(responseDto);

                // when & then
                mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                                .andExpect(jsonPath("$.destination.name").value("도쿄"))
                                .andExpect(jsonPath("$.startDate").value("2024.11.20"))
                                .andExpect(jsonPath("$.endDate").value("2024.11.23"))
                                .andExpect(jsonPath("$.participants").value(2));

                verify(travelPlanService, times(1)).createTravelPlan(any(CreateTravelPlanRequest.class));
        }

        @Test
        @DisplayName("여행 계획 생성 API 실패 - 필수 필드 누락")
        void createTravelPlan_ValidationFailed() throws Exception {
                // given
                CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                .title("") // 빈 제목
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .build();

                // when & then
                mockMvc.perform(post("/api/travel-plans")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());

                verify(travelPlanService, never()).createTravelPlan(any());
        }

        @Test
        @DisplayName("여행 계획 목록 조회 API 성공 (페이징)")
        void getTravelPlans_Success() throws Exception {
                // given
                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .isArchived(false)
                                .build();

                Page<TravelPlanDto> page = new PageImpl<>(
                                List.of(dto1),
                                PageRequest.of(0, 10),
                                1);

                when(travelPlanService.getTravelPlans(anyInt(), anyInt())).thenReturn(page);

                // when & then
                mockMvc.perform(get("/api/travel-plans")
                                .param("page", "0")
                                .param("limit", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].title").value("도쿄 여행"))
                                .andExpect(jsonPath("$.content[0].destination.name").value("도쿄"))
                                .andExpect(jsonPath("$.totalElements").value(1));

                verify(travelPlanService, times(1)).getTravelPlans(0, 10);
        }

        @Test
        @DisplayName("모든 여행 계획 조회 API 성공")
        void getAllTravelPlans_Success() throws Exception {
                // given
                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .build();

                when(travelPlanService.getAllTravelPlans()).thenReturn(List.of(dto1));

                // when & then
                mockMvc.perform(get("/api/travel-plans/all"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(1)))
                                .andExpect(jsonPath("$[0].title").value("도쿄 여행"));

                verify(travelPlanService, times(1)).getAllTravelPlans();
        }

        @Test
        @DisplayName("특정 여행 계획 조회 API 성공")
        void getTravelPlan_Success() throws Exception {
                // given
                Long planId = 1L;
                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                TravelPlanDto dto = TravelPlanDto.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .build();

                when(travelPlanService.getTravelPlanById(planId)).thenReturn(dto);

                // when & then
                mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(planId))
                                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                                .andExpect(jsonPath("$.destination.name").value("도쿄"));

                verify(travelPlanService, times(1)).getTravelPlanById(planId);
        }

        @Test
        @DisplayName("여행 계획 수정 API 성공")
        void updateTravelPlan_Success() throws Exception {
                // given
                Long planId = 1L;

                CityDto newDestinationDto = CityDto.builder()
                                .name("오사카")
                                .build();

                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .title("오사카 여행")
                                .destination(newDestinationDto)
                                .build();

                TravelPlanDto responseDto = TravelPlanDto.builder()
                                .id(planId)
                                .title("오사카 여행")
                                .destination(newDestinationDto)
                                .build();

                when(travelPlanService.updateTravelPlan(eq(planId), any(UpdateTravelPlanRequest.class)))
                                .thenReturn(responseDto);

                // when & then
                mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("오사카 여행"))
                                .andExpect(jsonPath("$.destination.name").value("오사카"));

                verify(travelPlanService, times(1)).updateTravelPlan(eq(planId),
                                any(UpdateTravelPlanRequest.class));
        }

        @Test
        @DisplayName("여행 계획 삭제 API 성공")
        void deleteTravelPlan_Success() throws Exception {
                // given
                Long planId = 1L;
                doNothing().when(travelPlanService).deleteTravelPlan(planId);

                // when & then
                mockMvc.perform(delete("/api/travel-plans/{planId}", planId))
                                .andExpect(status().isNoContent());

                verify(travelPlanService, times(1)).deleteTravelPlan(planId);
        }

        @Test
        @DisplayName("여행 계획 아카이브 API 성공")
        void archiveTravelPlan_Success() throws Exception {
                // given
                Long planId = 1L;

                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                TravelPlanDto responseDto = TravelPlanDto.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .isArchived(true)
                                .build();

                when(travelPlanService.archiveTravelPlan(planId)).thenReturn(responseDto);

                // when & then
                mockMvc.perform(post("/api/travel-plans/{planId}/archive", planId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(planId))
                                .andExpect(jsonPath("$.isArchived").value(true));

                verify(travelPlanService, times(1)).archiveTravelPlan(planId);
        }
}
