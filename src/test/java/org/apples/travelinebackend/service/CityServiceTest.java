package org.apples.travelinebackend.service;

import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    private City domesticCity1;
    private City domesticCity2;
    private City internationalCity1;
    private City internationalCity2;

    @BeforeEach
    void setUp() {
        domesticCity1 = City.builder()
                .id(1L)
                .name("서울")
                .isInternational(false)
                .build();

        domesticCity2 = City.builder()
                .id(2L)
                .name("부산")
                .isInternational(false)
                .build();

        internationalCity1 = City.builder()
                .id(3L)
                .name("도쿄")
                .isInternational(true)
                .build();

        internationalCity2 = City.builder()
                .id(4L)
                .name("오사카")
                .isInternational(true)
                .build();
    }

    @Test
    @DisplayName("모든 도시 조회")
    void getAllCities() {
        // Given
        List<City> allCities = Arrays.asList(
                domesticCity1, domesticCity2, internationalCity1, internationalCity2
        );
        when(cityRepository.findAll()).thenReturn(allCities);

        // When
        List<CityDto> result = cityService.getAllCities();

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).extracting("name")
                .containsExactly("서울", "부산", "도쿄", "오사카");
    }

    @Test
    @DisplayName("국내 도시만 조회")
    void getDomesticCities() {
        // Given
        List<City> allCities = Arrays.asList(
                domesticCity1, domesticCity2, internationalCity1, internationalCity2
        );
        when(cityRepository.findAll()).thenReturn(allCities);

        // When
        List<CityDto> result = cityService.getCitiesByType(false);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name")
                .containsExactly("서울", "부산");
        assertThat(result).allMatch(city -> !city.getIsInternational());
    }

    @Test
    @DisplayName("해외 도시만 조회")
    void getInternationalCities() {
        // Given
        List<City> allCities = Arrays.asList(
                domesticCity1, domesticCity2, internationalCity1, internationalCity2
        );
        when(cityRepository.findAll()).thenReturn(allCities);

        // When
        List<CityDto> result = cityService.getCitiesByType(true);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name")
                .containsExactly("도쿄", "오사카");
        assertThat(result).allMatch(CityDto::getIsInternational);
    }

    @Test
    @DisplayName("isInternational이 null이면 모든 도시 조회")
    void getCitiesWithNullFilter() {
        // Given
        List<City> allCities = Arrays.asList(
                domesticCity1, domesticCity2, internationalCity1, internationalCity2
        );
        when(cityRepository.findAll()).thenReturn(allCities);

        // When
        List<CityDto> result = cityService.getCitiesByType(null);

        // Then
        assertThat(result).hasSize(4);
        assertThat(result).extracting("name")
                .containsExactly("서울", "부산", "도쿄", "오사카");
    }
}

