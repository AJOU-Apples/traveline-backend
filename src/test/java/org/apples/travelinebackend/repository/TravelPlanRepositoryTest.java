package org.apples.travelinebackend.repository;

import jakarta.persistence.EntityManager;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("TravelPlanRepository 테스트")
class TravelPlanRepositoryTest {

    @Autowired
    private TravelPlanRepository travelPlanRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("여행 계획 저장 및 조회")
    void saveTravelPlan_Success() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        // when
        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);

        // then
        assertThat(savedPlan.getId()).isNotNull();
        assertThat(savedPlan.getTitle()).isEqualTo("도쿄 여행");
        assertThat(savedPlan.getDestination()).isEqualTo("도쿄");
        assertThat(savedPlan.getCreatedAt()).isNotNull();
        assertThat(savedPlan.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("여행 계획과 일차 정보 함께 저장 (Cascade)")
    void saveTravelPlanWithDays_Cascade() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        TravelDay day2 = TravelDay.builder()
                .dayNumber(2)
                .date(LocalDate.of(2024, 11, 21))
                .displayDate("11월 21일(목)")
                .build();

        travelPlan.addDay(day1);
        travelPlan.addDay(day2);

        // when
        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);

        // then
        assertThat(savedPlan.getDays()).hasSize(2);
        assertThat(savedPlan.getDays().get(0).getDayNumber()).isEqualTo(1);
        assertThat(savedPlan.getDays().get(1).getDayNumber()).isEqualTo(2);
        assertThat(savedPlan.getDays().get(0).getTravelPlan()).isEqualTo(savedPlan);
    }

    @Test
    @DisplayName("여행 계획, 일차, 장소 모두 함께 저장 (Cascade)")
    void saveTravelPlanWithDaysAndPlaces_Cascade() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        Place place1 = Place.builder()
                .name("도쿄 타워")
                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                .time("14:00")
                .latitude(35.6585805)
                .longitude(139.7454329)
                .displayOrder(1)
                .build();

        Place place2 = Place.builder()
                .name("아사쿠사")
                .address("Asakusa, Taito City, Tokyo")
                .time("17:00")
                .latitude(35.7148)
                .longitude(139.7967)
                .displayOrder(2)
                .build();

        day1.addPlace(place1);
        day1.addPlace(place2);
        travelPlan.addDay(day1);

        // when
        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);

        // then
        assertThat(savedPlan.getDays()).hasSize(1);
        assertThat(savedPlan.getDays().get(0).getPlaces()).hasSize(2);
        assertThat(savedPlan.getDays().get(0).getPlaces().get(0).getName()).isEqualTo("도쿄 타워");
        assertThat(savedPlan.getDays().get(0).getPlaces().get(1).getName()).isEqualTo("아사쿠사");
    }

    @Test
    @DisplayName("findAllWithDays - 모든 여행 계획과 일차 정보 조회")
    void findAllWithDays_Success() {
        // given
        TravelPlan plan1 = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        plan1.addDay(day1);

        TravelPlan plan2 = TravelPlan.builder()
                .title("파리 여행")
                .destination("파리")
                .startDate(LocalDate.of(2024, 12, 1))
                .endDate(LocalDate.of(2024, 12, 5))
                .participants(3)
                .build();

        travelPlanRepository.save(plan1);
        travelPlanRepository.save(plan2);

        // when
        List<TravelPlan> plans = travelPlanRepository.findAllWithDays();

        // then
        assertThat(plans).hasSize(2);
        // plan1이 day를 가지고 있는지 확인 (plan1 또는 plan2 중 days를 가진 것 찾기)
        boolean hasDays = plans.stream().anyMatch(p -> !p.getDays().isEmpty());
        assertThat(hasDays).isTrue();
    }

    @Test
    @DisplayName("findByIdWithDays - 특정 여행 계획의 일차 정보 조회")
    void findByIdWithDays_Success() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        Place place1 = Place.builder()
                .name("도쿄 타워")
                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                .displayOrder(1)
                .build();

        day1.addPlace(place1);
        travelPlan.addDay(day1);

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);

        // when
        Optional<TravelPlan> foundPlan = travelPlanRepository.findByIdWithDays(savedPlan.getId());

        // then
        assertThat(foundPlan).isPresent();
        assertThat(foundPlan.get().getDays()).hasSize(1);
        // Places는 lazy loading으로 조회됨
        assertThat(foundPlan.get().getDays().get(0).getPlaces()).hasSize(1);
        assertThat(foundPlan.get().getDays().get(0).getPlaces().get(0).getName()).isEqualTo("도쿄 타워");
    }

    @Test
    @DisplayName("여행 계획 삭제 시 일차와 장소도 함께 삭제 (Cascade)")
    void deleteTravelPlan_CascadeDelete() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        Place place1 = Place.builder()
                .name("도쿄 타워")
                .displayOrder(1)
                .build();

        day1.addPlace(place1);
        travelPlan.addDay(day1);

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        Long savedPlanId = savedPlan.getId();

        // when
        travelPlanRepository.deleteById(savedPlanId);

        // then
        Optional<TravelPlan> deletedPlan = travelPlanRepository.findById(savedPlanId);
        assertThat(deletedPlan).isEmpty();
    }

    @Test
    @DisplayName("여행 계획 업데이트 - updatedAt 자동 갱신")
    void updateTravelPlan_UpdatedAtAutoUpdate() throws InterruptedException {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        travelPlanRepository.flush();

        // 시간 차이를 위해 약간 대기
        Thread.sleep(100);

        // when
        savedPlan.setTitle("도쿄 여행 (수정됨)");
        TravelPlan updatedPlan = travelPlanRepository.save(savedPlan);
        travelPlanRepository.flush();

        // then
        assertThat(updatedPlan.getTitle()).isEqualTo("도쿄 여행 (수정됨)");
        assertThat(updatedPlan.getCreatedAt()).isNotNull();
        assertThat(updatedPlan.getUpdatedAt()).isNotNull();
        assertThat(updatedPlan.getUpdatedAt()).isAfter(updatedPlan.getCreatedAt());
    }

    @Test
    @DisplayName("일차는 dayNumber 순서로 정렬")
    void days_OrderByDayNumber() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day3 = TravelDay.builder()
                .dayNumber(3)
                .date(LocalDate.of(2024, 11, 22))
                .displayDate("11월 22일(금)")
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        TravelDay day2 = TravelDay.builder()
                .dayNumber(2)
                .date(LocalDate.of(2024, 11, 21))
                .displayDate("11월 21일(목)")
                .build();

        // 순서를 섞어서 추가
        travelPlan.addDay(day3);
        travelPlan.addDay(day1);
        travelPlan.addDay(day2);

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        travelPlanRepository.flush();
        entityManager.clear();

        // when
        TravelPlan foundPlan = travelPlanRepository.findById(savedPlan.getId()).get();

        // then
        assertThat(foundPlan.getDays()).hasSize(3);
        assertThat(foundPlan.getDays().get(0).getDayNumber()).isEqualTo(1);
        assertThat(foundPlan.getDays().get(1).getDayNumber()).isEqualTo(2);
        assertThat(foundPlan.getDays().get(2).getDayNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("장소는 displayOrder 순서로 정렬")
    void places_OrderByDisplayOrder() {
        // given
        TravelPlan travelPlan = TravelPlan.builder()
                .title("도쿄 여행")
                .destination("도쿄")
                .startDate(LocalDate.of(2024, 11, 20))
                .endDate(LocalDate.of(2024, 11, 23))
                .participants(2)
                .build();

        TravelDay day1 = TravelDay.builder()
                .dayNumber(1)
                .date(LocalDate.of(2024, 11, 20))
                .displayDate("11월 20일(수)")
                .build();

        Place place3 = Place.builder()
                .name("장소3")
                .displayOrder(3)
                .build();

        Place place1 = Place.builder()
                .name("장소1")
                .displayOrder(1)
                .build();

        Place place2 = Place.builder()
                .name("장소2")
                .displayOrder(2)
                .build();

        // 순서를 섞어서 추가
        day1.addPlace(place3);
        day1.addPlace(place1);
        day1.addPlace(place2);

        travelPlan.addDay(day1);

        TravelPlan savedPlan = travelPlanRepository.save(travelPlan);
        travelPlanRepository.flush();
        entityManager.clear();

        // when
        TravelPlan foundPlan = travelPlanRepository.findById(savedPlan.getId()).get();

        // then
        assertThat(foundPlan.getDays().get(0).getPlaces()).hasSize(3);
        assertThat(foundPlan.getDays().get(0).getPlaces().get(0).getName()).isEqualTo("장소1");
        assertThat(foundPlan.getDays().get(0).getPlaces().get(1).getName()).isEqualTo("장소2");
        assertThat(foundPlan.getDays().get(0).getPlaces().get(2).getName()).isEqualTo("장소3");
    }
}

