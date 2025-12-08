package org.apples.travelinebackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.service.CityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CityController.class)
class CityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CityService cityService;

    @Test
    @DisplayName("GET /api/cities - 모든 도시 조회")
    void getAllCities() throws Exception {
        // Given
        List<CityDto> cities = Arrays.asList(
                CityDto.builder().id(1L).name("서울").isInternational(false).build(),
                CityDto.builder().id(2L).name("부산").isInternational(false).build(),
                CityDto.builder().id(3L).name("도쿄").isInternational(true).build()
        );
        when(cityService.getCitiesByType(null)).thenReturn(cities);

        // When & Then
        mockMvc.perform(get("/api/cities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("서울"))
                .andExpect(jsonPath("$[0].isInternational").value(false))
                .andExpect(jsonPath("$[1].name").value("부산"))
                .andExpect(jsonPath("$[2].name").value("도쿄"))
                .andExpect(jsonPath("$[2].isInternational").value(true));
    }

    @Test
    @DisplayName("GET /api/cities?isInternational=false - 국내 도시만 조회")
    void getDomesticCities() throws Exception {
        // Given
        List<CityDto> domesticCities = Arrays.asList(
                CityDto.builder().id(1L).name("서울").isInternational(false).build(),
                CityDto.builder().id(2L).name("부산").isInternational(false).build()
        );
        when(cityService.getCitiesByType(false)).thenReturn(domesticCities);

        // When & Then
        mockMvc.perform(get("/api/cities")
                        .param("isInternational", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("서울"))
                .andExpect(jsonPath("$[0].isInternational").value(false))
                .andExpect(jsonPath("$[1].name").value("부산"))
                .andExpect(jsonPath("$[1].isInternational").value(false));
    }

    @Test
    @DisplayName("GET /api/cities?isInternational=true - 해외 도시만 조회")
    void getInternationalCities() throws Exception {
        // Given
        List<CityDto> internationalCities = Arrays.asList(
                CityDto.builder().id(3L).name("도쿄").isInternational(true).build(),
                CityDto.builder().id(4L).name("오사카").isInternational(true).build()
        );
        when(cityService.getCitiesByType(true)).thenReturn(internationalCities);

        // When & Then
        mockMvc.perform(get("/api/cities")
                        .param("isInternational", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("도쿄"))
                .andExpect(jsonPath("$[0].isInternational").value(true))
                .andExpect(jsonPath("$[1].name").value("오사카"))
                .andExpect(jsonPath("$[1].isInternational").value(true));
    }
}

