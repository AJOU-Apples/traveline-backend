package org.apples.travelinebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.service.TravelPlanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TravelPlanController.class)
@DisplayName("TravelPlanController 통합 테스트")
class TravelPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TravelPlanService travelPlanService;

    @Test
    @DisplayName("POST /api/travel-plans - 여행 계획 생성 성공")
    void createTravelPlan_Success() throws Exception {
        // given
        CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(new ArrayList<>())
                .build();

        TravelPlanDto response = TravelPlanDto.builder()
                .id(1L)
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(new ArrayList<>())
                .build();

        when(travelPlanService.createTravelPlan(any(CreateTravelPlanRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                .andExpect(jsonPath("$.destination").value("도쿄"))
                .andExpect(jsonPath("$.startDate").value("2024.11.20"))
                .andExpect(jsonPath("$.endDate").value("2024.11.23"))
                .andExpect(jsonPath("$.participants").value(2));

        verify(travelPlanService, times(1)).createTravelPlan(any(CreateTravelPlanRequest.class));
    }

    @Test
    @DisplayName("POST /api/travel-plans - 필수 필드 누락 시 실패")
    void createTravelPlan_ValidationFail() throws Exception {
        // given
        CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                // title 누락
                .participants(2)
                .build();

        // when & then
        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(travelPlanService, never()).createTravelPlan(any());
    }

    @Test
    @DisplayName("POST /api/travel-plans - 참가자 수 0 이하 시 실패")
    void createTravelPlan_InvalidParticipants() throws Exception {
        // given
        CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(0) // 최소 1명 이상
                .build();

        // when & then
        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(travelPlanService, never()).createTravelPlan(any());
    }

    @Test
    @DisplayName("GET /api/travel-plans/all - 모든 여행 계획 조회 성공 (레거시)")
    void getAllTravelPlans_Success() throws Exception {
        // given
        List<TravelPlanDto> plans = List.of(
                TravelPlanDto.builder()
                        .id(1L)
                        .title("도쿄 여행")
                        .destination("도쿄")
                        .startDate("2024.11.20")
                        .endDate("2024.11.23")
                        .participants(2)
                        .days(new ArrayList<>())
                        .build(),
                TravelPlanDto.builder()
                        .id(2L)
                        .title("파리 여행")
                        .destination("파리")
                        .startDate("2024.12.01")
                        .endDate("2024.12.05")
                        .participants(3)
                        .days(new ArrayList<>())
                        .build()
        );

        when(travelPlanService.getAllTravelPlans()).thenReturn(plans);

        // when & then
        mockMvc.perform(get("/api/travel-plans/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("도쿄 여행"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("파리 여행"));

        verify(travelPlanService, times(1)).getAllTravelPlans();
    }

    @Test
    @DisplayName("GET /api/travel-plans/all - 빈 목록 반환 (레거시)")
    void getAllTravelPlans_EmptyList() throws Exception {
        // given
        when(travelPlanService.getAllTravelPlans()).thenReturn(new ArrayList<>());

        // when & then
        mockMvc.perform(get("/api/travel-plans/all"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(travelPlanService, times(1)).getAllTravelPlans();
    }

    @Test
    @DisplayName("GET /api/travel-plans/{planId} - 특정 여행 계획 조회 성공")
    void getTravelPlan_Success() throws Exception {
        // given
        Long planId = 1L;
        TravelDayDto dayDto = TravelDayDto.builder()
                .id(1L)
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(new ArrayList<>())
                .build();

        TravelPlanDto response = TravelPlanDto.builder()
                .id(planId)
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(dayDto))
                .build();

        when(travelPlanService.getTravelPlanById(planId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].dayNumber").value(1));

        verify(travelPlanService, times(1)).getTravelPlanById(planId);
    }

    @Test
    @DisplayName("GET /api/travel-plans/{planId} - 존재하지 않는 계획 조회 시 404")
    void getTravelPlan_NotFound() throws Exception {
        // given
        Long planId = 999L;
        when(travelPlanService.getTravelPlanById(planId))
                .thenThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + planId));

        // when & then
        mockMvc.perform(get("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("여행 계획을 찾을 수 없습니다. ID: 999"));

        verify(travelPlanService, times(1)).getTravelPlanById(planId);
    }

    @Test
    @DisplayName("PUT /api/travel-plans/{planId} - 여행 계획 수정 성공")
    void updateTravelPlan_Success() throws Exception {
        // given
        Long planId = 1L;
        UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                .title("도쿄 여행 (수정됨)")
                .participants(3)
                .build();

        TravelPlanDto response = TravelPlanDto.builder()
                .id(planId)
                .title("도쿄 여행 (수정됨)")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(3)
                .days(new ArrayList<>())
                .build();

        when(travelPlanService.updateTravelPlan(eq(planId), any(UpdateTravelPlanRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("도쿄 여행 (수정됨)"))
                .andExpect(jsonPath("$.participants").value(3));

        verify(travelPlanService, times(1)).updateTravelPlan(eq(planId), any(UpdateTravelPlanRequest.class));
    }

    @Test
    @DisplayName("PUT /api/travel-plans/{planId} - 존재하지 않는 계획 수정 시 404")
    void updateTravelPlan_NotFound() throws Exception {
        // given
        Long planId = 999L;
        UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                .title("도쿄 여행 (수정됨)")
                .build();

        when(travelPlanService.updateTravelPlan(eq(planId), any(UpdateTravelPlanRequest.class)))
                .thenThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + planId));

        // when & then
        mockMvc.perform(put("/api/travel-plans/{planId}", planId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(travelPlanService, times(1)).updateTravelPlan(eq(planId), any(UpdateTravelPlanRequest.class));
    }

    @Test
    @DisplayName("DELETE /api/travel-plans/{planId} - 여행 계획 삭제 성공")
    void deleteTravelPlan_Success() throws Exception {
        // given
        Long planId = 1L;
        doNothing().when(travelPlanService).deleteTravelPlan(planId);

        // when & then
        mockMvc.perform(delete("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(travelPlanService, times(1)).deleteTravelPlan(planId);
    }

    @Test
    @DisplayName("DELETE /api/travel-plans/{planId} - 존재하지 않는 계획 삭제 시 404")
    void deleteTravelPlan_NotFound() throws Exception {
        // given
        Long planId = 999L;
        doThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + planId))
                .when(travelPlanService).deleteTravelPlan(planId);

        // when & then
        mockMvc.perform(delete("/api/travel-plans/{planId}", planId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(travelPlanService, times(1)).deleteTravelPlan(planId);
    }

    @Test
    @DisplayName("POST /api/travel-plans - 장소 정보 포함 여행 계획 생성")
    void createTravelPlan_WithPlaces() throws Exception {
        // given
        PlaceDto placeDto = PlaceDto.builder()
                .name("도쿄 타워")
                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                .time("14:00")
                .latitude(35.6585805)
                .longitude(139.7454329)
                .build();

        TravelDayDto dayDto = TravelDayDto.builder()
                .dayNumber(1)
                .date("2024-11-20")
                .displayDate("11월 20일(수)")
                .places(List.of(placeDto))
                .build();

        CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(dayDto))
                .build();

        TravelPlanDto response = TravelPlanDto.builder()
                .id(1L)
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .days(List.of(
                        TravelDayDto.builder()
                                .id(1L)
                                .dayNumber(1)
                                .date("2024-11-20")
                                .displayDate("11월 20일(수)")
                                .places(List.of(
                                        PlaceDto.builder()
                                                .id(1L)
                                                .name("도쿄 타워")
                                                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                                                .time("14:00")
                                                .latitude(35.6585805)
                                                .longitude(139.7454329)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        when(travelPlanService.createTravelPlan(any(CreateTravelPlanRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/travel-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.days[0].places", hasSize(1)))
                .andExpect(jsonPath("$.days[0].places[0].name").value("도쿄 타워"))
                .andExpect(jsonPath("$.days[0].places[0].latitude").value(35.6585805));

        verify(travelPlanService, times(1)).createTravelPlan(any(CreateTravelPlanRequest.class));
    }
    
    @Test
    @DisplayName("GET /api/travel-plans - 페이징 및 필터링 (status 없음)")
    void getTravelPlans_WithPaging() throws Exception {
        // given
        List<TravelPlanDto> plans = List.of(
                TravelPlanDto.builder()
                        .id(1L)
                        .title("도쿄 여행")
                        .destination("도쿄")
                        .startDate("2024.11.20")
                        .endDate("2024.11.23")
                        .participants(2)
                        .days(new ArrayList<>())
                        .build(),
                TravelPlanDto.builder()
                        .id(2L)
                        .title("파리 여행")
                        .destination("파리")
                        .startDate("2024.12.01")
                        .endDate("2024.12.05")
                        .participants(3)
                        .days(new ArrayList<>())
                        .build()
        );
        
        Page<TravelPlanDto> planPage = new PageImpl<>(plans, PageRequest.of(0, 10), 2);
        
        when(travelPlanService.getTravelPlans(null, 0, 10)).thenReturn(planPage);

        // when & then
        mockMvc.perform(get("/api/travel-plans")
                        .param("page", "0")
                        .param("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("도쿄 여행"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("파리 여행"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(travelPlanService, times(1)).getTravelPlans(null, 0, 10);
    }
    
    @Test
    @DisplayName("GET /api/travel-plans - 페이징 및 필터링 (status=upcoming)")
    void getTravelPlans_WithStatusFilter() throws Exception {
        // given
        List<TravelPlanDto> plans = List.of(
                TravelPlanDto.builder()
                        .id(1L)
                        .title("도쿄 여행")
                        .destination("도쿄")
                        .startDate("2024.11.20")
                        .endDate("2024.11.23")
                        .participants(2)
                        .days(new ArrayList<>())
                        .build()
        );
        
        Page<TravelPlanDto> planPage = new PageImpl<>(plans, PageRequest.of(0, 10), 1);
        
        when(travelPlanService.getTravelPlans("upcoming", 0, 10)).thenReturn(planPage);

        // when & then
        mockMvc.perform(get("/api/travel-plans")
                        .param("status", "upcoming")
                        .param("page", "0")
                        .param("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("도쿄 여행"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(travelPlanService, times(1)).getTravelPlans("upcoming", 0, 10);
    }
    
    @Test
    @DisplayName("POST /api/travel-plans/{planId}/archive - 여행 계획 아카이브 성공")
    void archiveTravelPlan_Success() throws Exception {
        // given
        Long planId = 1L;
        TravelPlanDto response = TravelPlanDto.builder()
                .id(planId)
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate("2024.11.20")
                .endDate("2024.11.23")
                .participants(2)
                .isArchived(true)
                .days(new ArrayList<>())
                .build();

        when(travelPlanService.archiveTravelPlan(planId)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/travel-plans/{planId}/archive", planId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("도쿄 여행"))
                .andExpect(jsonPath("$.isArchived").value(true));

        verify(travelPlanService, times(1)).archiveTravelPlan(planId);
    }
    
    @Test
    @DisplayName("POST /api/travel-plans/{planId}/archive - 존재하지 않는 계획 아카이브 시 404")
    void archiveTravelPlan_NotFound() throws Exception {
        // given
        Long planId = 999L;
        when(travelPlanService.archiveTravelPlan(planId))
                .thenThrow(new IllegalArgumentException("여행 계획을 찾을 수 없습니다. ID: " + planId));

        // when & then
        mockMvc.perform(post("/api/travel-plans/{planId}/archive", planId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(travelPlanService, times(1)).archiveTravelPlan(planId);
    }
}

