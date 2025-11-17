package org.apples.travelinebackend.service;

import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.CityDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.City;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
import org.apples.travelinebackend.repository.CityRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelPlanService 단위 테스트")
class TravelPlanServiceTest {

        @Mock
        private TravelPlanRepository travelPlanRepository;

        @Mock
        private CityRepository CityRepository;

        @Mock
        private TravelPlanMapper travelPlanMapper;

        @InjectMocks
        private TravelPlanService travelPlanService;

        @Test
        @DisplayName("여행 계획 생성 성공")
        void createTravelPlan_Success() {
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

                City savedDestination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .latitude(35.6762)
                                .longitude(139.6503)
                                .build();

                TravelPlan savedPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(savedDestination)
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .build();

                when(CityRepository.save(any(City.class))).thenReturn(savedDestination);
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(savedPlan);
                when(travelPlanMapper.toDto(savedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.createTravelPlan(request);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("도쿄 여행");
                assertThat(result.getDestination()).isNotNull();
                assertThat(result.getDestination().getName()).isEqualTo("도쿄");

                verify(CityRepository, times(1)).save(any(City.class));
                verify(travelPlanRepository, times(1)).save(any(TravelPlan.class));
        }

        @Test
        @DisplayName("여행 계획 생성 - 일차 정보 포함")
        void createTravelPlan_WithDays() {
                // given
                TravelDayDto dayDto = TravelDayDto.builder()
                                .dayNumber(1)
                                .date("2024-11-20")
                                .displayDate("11월 20일(수)")
                                .build();

                CityDto destinationDto = CityDto.builder()
                                .name("도쿄")
                                .build();

                CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination(destinationDto)
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(List.of(dayDto))
                                .build();

                City savedDestination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan savedPlan = TravelPlan.builder()
                                .id(1L)
                                .build();

                when(CityRepository.save(any(City.class))).thenReturn(savedDestination);
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(savedPlan);
                when(travelPlanMapper.toDto(any(TravelPlan.class))).thenReturn(new TravelPlanDto());

                // when
                travelPlanService.createTravelPlan(request);

                // then
                ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
                verify(travelPlanRepository).save(captor.capture());

                TravelPlan capturedPlan = captor.getValue();
                assertThat(capturedPlan.getDays()).hasSize(1);
                assertThat(capturedPlan.getDays().get(0).getDayNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("모든 여행 계획 조회 성공")
        void getAllTravelPlans_Success() {
                // given
                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan plan1 = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destination)
                                .build();

                List<TravelPlan> plans = List.of(plan1);

                when(travelPlanRepository.findAllWithDays()).thenReturn(plans);
                when(travelPlanMapper.toDto(any(TravelPlan.class))).thenReturn(new TravelPlanDto());

                // when
                List<TravelPlanDto> result = travelPlanService.getAllTravelPlans();

                // then
                assertThat(result).hasSize(1);
                verify(travelPlanRepository, times(1)).findAllWithDays();
        }

        @Test
        @DisplayName("페이징된 여행 계획 조회 성공")
        void getTravelPlans_Success() {
                // given
                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan plan1 = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination(destination)
                                .isArchived(false)
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .build();

                Page<TravelPlan> planPage = new PageImpl<>(List.of(plan1), PageRequest.of(0, 10), 1);

                when(travelPlanRepository.findByIsArchivedFalse(any(PageRequest.class)))
                                .thenReturn(planPage);
                when(travelPlanMapper.toDto(plan1)).thenReturn(dto1);

                // when
                Page<TravelPlanDto> result = travelPlanService.getTravelPlans(0, 10);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getTitle()).isEqualTo("도쿄 여행");

                verify(travelPlanRepository, times(1))
                                .findByIsArchivedFalse(any(PageRequest.class));
        }

        @Test
        @DisplayName("특정 여행 계획 조회 성공")
        void getTravelPlanById_Success() {
                // given
                Long planId = 1L;

                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan plan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destination)
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .build();

                when(travelPlanRepository.findByIdWithDays(planId)).thenReturn(Optional.of(plan));
                when(travelPlanMapper.toDto(plan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.getTravelPlanById(planId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(planId);
                assertThat(result.getTitle()).isEqualTo("도쿄 여행");

                verify(travelPlanRepository, times(1)).findByIdWithDays(planId);
        }

        @Test
        @DisplayName("존재하지 않는 여행 계획 조회 시 예외 발생")
        void getTravelPlanById_NotFound() {
                // given
                Long planId = 999L;
                when(travelPlanRepository.findByIdWithDays(planId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> travelPlanService.getTravelPlanById(planId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다");

                verify(travelPlanRepository, times(1)).findByIdWithDays(planId);
        }

        @Test
        @DisplayName("여행 계획 업데이트 성공")
        void updateTravelPlan_Success() {
                // given
                Long planId = 1L;

                City originalDestination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan existingPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(originalDestination)
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .build();

                CityDto newDestinationDto = CityDto.builder()
                                .name("오사카")
                                .build();

                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .title("오사카 여행")
                                .destination(newDestinationDto)
                                .build();

                City newDestination = City.builder()
                                .id(2L)
                                .name("오사카")
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
                when(CityRepository.save(any(City.class))).thenReturn(newDestination);
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(existingPlan);
                when(travelPlanMapper.toDto(any(TravelPlan.class))).thenReturn(new TravelPlanDto());

                // when
                travelPlanService.updateTravelPlan(planId, request);

                // then
                ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
                verify(travelPlanRepository).save(captor.capture());

                TravelPlan updatedPlan = captor.getValue();
                assertThat(updatedPlan.getTitle()).isEqualTo("오사카 여행");
                assertThat(updatedPlan.getDestination().getName()).isEqualTo("오사카");
        }

        @Test
        @DisplayName("존재하지 않는 여행 계획 업데이트 시 예외 발생")
        void updateTravelPlan_NotFound() {
                // given
                Long planId = 999L;
                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .title("수정된 제목")
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> travelPlanService.updateTravelPlan(planId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다");

                verify(travelPlanRepository, times(1)).findById(planId);
        }

        @Test
        @DisplayName("여행 계획 삭제 성공 (Soft Delete)")
        void deleteTravelPlan_Success() {
                // given
                Long planId = 1L;

                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan plan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destination)
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(plan));

                // when
                travelPlanService.deleteTravelPlan(planId);

                // then
                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, times(1)).delete(plan);
        }

        @Test
        @DisplayName("존재하지 않는 여행 계획 삭제 시 예외 발생")
        void deleteTravelPlan_NotFound() {
                // given
                Long planId = 999L;
                when(travelPlanRepository.findById(planId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> travelPlanService.deleteTravelPlan(planId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다");

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, never()).delete(any());
        }

        @Test
        @DisplayName("여행 계획 아카이브 성공")
        void archiveTravelPlan_Success() {
                // given
                Long planId = 1L;

                City destination = City.builder()
                                .id(1L)
                                .name("도쿄")
                                .build();

                TravelPlan plan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destination)
                                .isArchived(false)
                                .build();

                TravelPlan archivedPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination(destination)
                                .isArchived(true)
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(planId)
                                .isArchived(true)
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(archivedPlan);
                when(travelPlanMapper.toDto(archivedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.archiveTravelPlan(planId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getIsArchived()).isTrue();

                ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
                verify(travelPlanRepository).save(captor.capture());
                assertThat(captor.getValue().getIsArchived()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 여행 계획 아카이브 시 예외 발생")
        void archiveTravelPlan_NotFound() {
                // given
                Long planId = 999L;
                when(travelPlanRepository.findById(planId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> travelPlanService.archiveTravelPlan(planId))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다");

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, never()).save(any());
        }
}
