package org.apples.travelinebackend.controller;

import lombok.RequiredArgsConstructor;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.service.CityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    /**
     * 도시 목록 조회
     * @param isInternational 선택적 필터: true(해외), false(국내), null(전체)
     * @return 도시 목록
     */
    @GetMapping
    public ResponseEntity<List<CityDto>> getCities(
            @RequestParam(required = false) Boolean isInternational) {
        List<CityDto> cities = cityService.getCitiesByType(isInternational);
        return ResponseEntity.ok(cities);
    }
}

