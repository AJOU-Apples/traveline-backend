package org.apples.travelinebackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.repository.CityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CityRepository cityRepository;

    @Override
    public void run(String... args) {
        // 이미 데이터가 있으면 초기화하지 않음
        if (cityRepository.count() > 0) {
            log.info("Cities already initialized. Skipping data initialization.");
            return;
        }

        log.info("Initializing cities data...");

        List<City> cities = new ArrayList<>();

        // 국내 여행지
        cities.add(City.builder().name("서울").isInternational(false).latitude(37.5665).longitude(126.9780).build());
        cities.add(City.builder().name("부산").isInternational(false).latitude(35.1796).longitude(129.0756).build());
        cities.add(City.builder().name("제주").isInternational(false).latitude(33.4996).longitude(126.5312).build());
        cities.add(City.builder().name("강릉").isInternational(false).latitude(37.7519).longitude(128.8761).build());
        cities.add(City.builder().name("여수").isInternational(false).latitude(34.7604).longitude(127.6622).build());

        // 국제 여행지 - 일본
        cities.add(City.builder().name("도쿄").isInternational(true).latitude(35.6762).longitude(139.6503).build());
        cities.add(City.builder().name("오사카").isInternational(true).latitude(34.6937).longitude(135.5023).build());
        cities.add(City.builder().name("후쿠오카").isInternational(true).latitude(33.5904).longitude(130.4017).build());
        cities.add(City.builder().name("가고시마").isInternational(true).latitude(31.5969).longitude(130.5571).build());
        cities.add(City.builder().name("삿포로").isInternational(true).latitude(43.0642).longitude(141.3469).build());
        cities.add(City.builder().name("시즈오카").isInternational(true).latitude(34.9756).longitude(138.3828).build());
        cities.add(City.builder().name("나고야").isInternational(true).latitude(35.1815).longitude(136.9066).build());
        cities.add(City.builder().name("오키나와").isInternational(true).latitude(26.2124).longitude(127.6809).build());
        cities.add(City.builder().name("마쓰야마").isInternational(true).latitude(33.8392).longitude(132.7658).build());
        cities.add(City.builder().name("구마모토").isInternational(true).latitude(32.8031).longitude(130.7079).build());

        // 국제 여행지 - 동남아시아
        cities.add(City.builder().name("나트랑").isInternational(true).latitude(12.2388).longitude(109.1967).build());
        cities.add(City.builder().name("마닐라").isInternational(true).latitude(14.5995).longitude(120.9842).build());
        cities.add(City.builder().name("미얀마").isInternational(true).latitude(21.9162).longitude(95.9560).build());
        cities.add(City.builder().name("치앙마이").isInternational(true).latitude(18.7883).longitude(98.9853).build());
        cities.add(City.builder().name("방콕").isInternational(true).latitude(13.7563).longitude(100.5018).build());
        cities.add(City.builder().name("하노이").isInternational(true).latitude(21.0285).longitude(105.8542).build());
        cities.add(City.builder().name("하롱베이").isInternational(true).latitude(20.9101).longitude(107.1839).build());
        cities.add(City.builder().name("호치민").isInternational(true).latitude(10.8231).longitude(106.6297).build());

        // 국제 여행지 - 기타
        cities.add(City.builder().name("하이델베르크").isInternational(true).latitude(49.3988).longitude(8.6724).build());

        cityRepository.saveAll(cities);

        log.info("Successfully initialized {} cities", cities.size());
    }
}
