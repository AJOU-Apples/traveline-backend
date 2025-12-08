package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.entity.City;
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
                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .isInternational(true)
                                .latitude(35.6762)
                                .longitude(139.6503)
                                .build();
                
                TravelPlan entity = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destination)
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
                assertThat(dto.getDestination()).isNotNull();
                assertThat(dto.getDestination().getName()).isEqualTo("도쿄");
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
        @DisplayName("City Entity를 DTO로 변환")
        void toDto_City() {
                // given
                City entity = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .isInternational(true)
                                .latitude(35.6762)
                                .longitude(139.6503)
                                .build();

                // when
                CityDto dto = mapper.toCityDto(entity);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getName()).isEqualTo("도쿄");
                assertThat(dto.getIsInternational()).isTrue();
                assertThat(dto.getLatitude()).isEqualTo(35.6762);
                assertThat(dto.getLongitude()).isEqualTo(139.6503);
        }

        @Test
        @DisplayName("TravelPlan with Days를 DTO로 변환")
        void toDto_TravelPlanWithDays() {
                // given
                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .isInternational(true)
                                .build();
                
                TravelPlan travelPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destination)
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
        @DisplayName("null Entity는 null DTO 반환")
        void toDto_NullEntity() {
                // when & then
                assertThat(mapper.toDto(null)).isNull();
                assertThat(mapper.toDayDto(null)).isNull();
                assertThat(mapper.toCityDto(null)).isNull();
        }

        @Test
        @DisplayName("전체 계층 구조 변환")
        void toDto_FullHierarchy() {
                // given
                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .isInternational(true)
                                .latitude(35.6762)
                                .longitude(139.6503)
                                .build();
                
                TravelPlan travelPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destination)
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

                TravelDay day2 = TravelDay.builder()
                                .id(2L)
                                .dayNumber(2)
                                .date(LocalDate.of(2024, 11, 21))
                                .displayDate("11월 21일(목)")
                                .build();

                travelPlan.addDay(day1);
                travelPlan.addDay(day2);

                // when
                TravelPlanDto dto = mapper.toDto(travelPlan);

                // then
                assertThat(dto).isNotNull();
                assertThat(dto.getId()).isEqualTo(1L);
                assertThat(dto.getDestination().getName()).isEqualTo("도쿄");
                assertThat(dto.getDays()).hasSize(2);
                assertThat(dto.getDays().get(0).getDayNumber()).isEqualTo(1);
                assertThat(dto.getDays().get(1).getDayNumber()).isEqualTo(2);
        }
}
