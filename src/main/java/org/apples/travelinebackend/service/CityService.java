package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;

    /**
     * 모든 도시 조회
     */
    public List<CityDto> getAllCities() {
        log.info("Fetching all cities");
        List<City> cities = cityRepository.findAll();
        return cities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 국내/해외 구분으로 도시 조회
     * 
     * @param isInternational null이면 모든 도시, true면 해외, false면 국내
     */
    public List<CityDto> getCitiesByType(Boolean isInternational) {
        if (isInternational == null) {
            return getAllCities();
        }

        log.info("Fetching {} cities", isInternational ? "international" : "domestic");
        List<City> cities = cityRepository.findAll().stream()
                .filter(city -> city.getIsInternational().equals(isInternational))
                .collect(Collectors.toList());

        return cities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * City 엔티티를 CityDto로 변환
     */
    private CityDto convertToDto(City city) {
        return CityDto.builder()
                .id(city.getId())
                .name(city.getName())
                .isInternational(city.getIsInternational())
                .latitude(city.getLatitude())
                .longitude(city.getLongitude())
                .build();
    }
}

