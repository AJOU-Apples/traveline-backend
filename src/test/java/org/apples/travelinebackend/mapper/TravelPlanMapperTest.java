package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.Place;
import org.apples.travelinebackend.entity.TravelDay;
import org.apples.travelinebackend.entity.TravelPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TravelPlanMapper 테스트")
class TravelPlanMapperTest {

        private TravelPlanMapper mapper;

        @BeforeEach
        void setUp() {
                mapper = new TravelPlanMapper();
        }

        @Test
        @DisplayName("TravelPlan Entity를 DTO로 변환")
        void toDto_TravelPlan() {
                // given
                TravelPlan entity = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                // when
                TravelPlanDto dto = mapper.toDto(entity);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getTitle()).isEqualTo("도쿄 여행");
                assertThat(dto.getDestination()).isEqualTo("도쿄");
                assertThat(dto.getStartDate()).isEqualTo("2024.11.20");
                assertThat(dto.getEndDate()).isEqualTo("2024.11.23");
                assertThat(dto.getParticipants()).isEqualTo(2);
        }

        @Test
        @DisplayName("TravelDay Entity를 DTO로 변환")
        void toDto_TravelDay() {
                // given
                TravelDay entity = TravelDay.builder()
                                .id(1L)
                                .dayNumber(1)
                                .date(LocalDate.of(2024, 11, 20))
                                .displayDate("11월 20일(수)")
                                .places(new ArrayList<>())
                                .build();

                // when
                TravelDayDto dto = mapper.toDayDto(entity);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getDayNumber()).isEqualTo(1);
                assertThat(dto.getDate()).isEqualTo("2024-11-20");
                assertThat(dto.getDisplayDate()).isEqualTo("11월 20일(수)");
        }

        @Test
        @DisplayName("Place Entity를 DTO로 변환")
        void toDto_Place() {
                // given
                Place entity = Place.builder()
                                .id(1L)
                                .name("도쿄 타워")
                                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                                .time("14:00")
                                .memo("입장료 1,200엔")
                                .latitude(35.6585805)
                                .longitude(139.7454329)
                                .build();

                // when
                PlaceDto dto = mapper.toPlaceDto(entity);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getName()).isEqualTo("도쿄 타워");
                assertThat(dto.getAddress()).isEqualTo("4 Chome-2-8 Shibakoen, Minato City, Tokyo");
                assertThat(dto.getTime()).isEqualTo("14:00");
                assertThat(dto.getMemo()).isEqualTo("입장료 1,200엔");
                assertThat(dto.getLatitude()).isEqualTo(35.6585805);
                assertThat(dto.getLongitude()).isEqualTo(139.7454329);
        }

        @Test
        @DisplayName("TravelPlan with Days를 DTO로 변환")
        void toDto_TravelPlanWithDays() {
                // given
                TravelPlan travelPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .build();

                TravelDay day1 = TravelDay.builder()
                                .id(1L)
                                .dayNumber(1)
                                .date(LocalDate.of(2024, 11, 20))
                                .displayDate("11월 20일(수)")
                                .build();

                travelPlan.addDay(day1);

                // when
                TravelPlanDto dto = mapper.toDto(travelPlan);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getDays()).hasSize(1);
                assertThat(dto.getDays().get(0).getDayNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("TravelDay with Places를 DTO로 변환")
        void toDto_TravelDayWithPlaces() {
                // given
                TravelDay day = TravelDay.builder()
                                .id(1L)
                                .dayNumber(1)
                                .date(LocalDate.of(2024, 11, 20))
                                .displayDate("11월 20일(수)")
                                .build();

                Place place1 = Place.builder()
                                .id(1L)
                                .name("도쿄 타워")
                                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                                .build();

                day.addPlace(place1);

                // when
                TravelDayDto dto = mapper.toDayDto(day);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getPlaces()).hasSize(1);
                assertThat(dto.getPlaces().get(0).getName()).isEqualTo("도쿄 타워");
        }

        @Test
        @DisplayName("null Entity는 null DTO 반환")
        void toDto_NullEntity() {
                // when & then
                assertThat(mapper.toDto(null)).isNull();
                assertThat(mapper.toDayDto(null)).isNull();
                assertThat(mapper.toPlaceDto(null)).isNull();
        }

        @Test
        @DisplayName("전체 계층 구조 변환")
        void toDto_FullHierarchy() {
                // given
                TravelPlan travelPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .build();

                TravelDay day1 = TravelDay.builder()
                                .id(1L)
                                .dayNumber(1)
                                .date(LocalDate.of(2024, 11, 20))
                                .displayDate("11월 20일(수)")
                                .build();

                Place place1 = Place.builder()
                                .id(1L)
                                .name("도쿄 타워")
                                .address("4 Chome-2-8 Shibakoen, Minato City, Tokyo")
                                .time("14:00")
                                .latitude(35.6585805)
                                .longitude(139.7454329)
                                .build();

                Place place2 = Place.builder()
                                .id(2L)
                                .name("아사쿠사")
                                .address("Asakusa, Taito City, Tokyo")
                                .time("17:00")
                                .latitude(35.7148)
                                .longitude(139.7967)
                                .build();

                day1.addPlace(place1);
                day1.addPlace(place2);
                travelPlan.addDay(day1);

                // when
                TravelPlanDto dto = mapper.toDto(travelPlan);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getDays()).hasSize(1);
                assertThat(dto.getDays().get(0).getPlaces()).hasSize(2);
                assertThat(dto.getDays().get(0).getPlaces().get(0).getName()).isEqualTo("도쿄 타워");
                assertThat(dto.getDays().get(0).getPlaces().get(1).getName()).isEqualTo("아사쿠사");
        }
}
