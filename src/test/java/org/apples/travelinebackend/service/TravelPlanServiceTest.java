package org.apples.travelinebackend.service;

import org.apples.travelinebackend.dto.CreateTravelPlanRequest;
import org.apples.travelinebackend.dto.PlaceDto;
import org.apples.travelinebackend.dto.TravelDayDto;
import org.apples.travelinebackend.dto.TravelPlanDto;
import org.apples.travelinebackend.dto.UpdateTravelPlanRequest;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.TravelPlanStatus;
import org.apples.travelinebackend.mapper.TravelPlanMapper;
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
        private TravelPlanMapper travelPlanMapper;

        @InjectMocks
        private TravelPlanService travelPlanService;

        @Test
        @DisplayName("여행 계획 생성 성공")
        void createTravelPlan_Success() {
                // given
                CreateTravelPlanRequest request = CreateTravelPlanRequest.builder()
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                TravelPlan savedPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate("2024.11.20")
                                .endDate("2024.11.23")
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(savedPlan);
                when(travelPlanMapper.toDto(savedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.createTravelPlan(request);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(1L);
                assertThat(result.getTitle()).isEqualTo("도쿄 여행");
                assertThat(result.getDestination()).isEqualTo("도쿄");
                assertThat(result.getParticipants()).isEqualTo(2);

                verify(travelPlanRepository, times(1)).save(any(TravelPlan.class));
                verify(travelPlanMapper, times(1)).toDto(savedPlan);
        }

        @Test
        @DisplayName("여행 계획 생성 - 일차 및 장소 포함")
        void createTravelPlan_WithDaysAndPlaces() {
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

                TravelPlan savedPlan = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .build();

                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(savedPlan);
                when(travelPlanMapper.toDto(savedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.createTravelPlan(request);

                // then
                assertThat(result).isNotNull();

                ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
                verify(travelPlanRepository, times(1)).save(captor.capture());

                TravelPlan capturedPlan = captor.getValue();
                assertThat(capturedPlan.getDays()).hasSize(1);
                assertThat(capturedPlan.getDays().get(0).getPlaces()).hasSize(1);
                assertThat(capturedPlan.getDays().get(0).getPlaces().get(0).getName()).isEqualTo("도쿄 타워");
        }

        @Test
        @DisplayName("모든 여행 계획 조회")
        void getAllTravelPlans_Success() {
                // given
                TravelPlan plan1 = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .build();

                TravelPlan plan2 = TravelPlan.builder()
                                .id(2L)
                                .title("파리 여행")
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .build();

                TravelPlanDto dto2 = TravelPlanDto.builder()
                                .id(2L)
                                .title("파리 여행")
                                .build();

                when(travelPlanRepository.findAllWithDays()).thenReturn(List.of(plan1, plan2));
                when(travelPlanMapper.toDto(plan1)).thenReturn(dto1);
                when(travelPlanMapper.toDto(plan2)).thenReturn(dto2);

                // when
                List<TravelPlanDto> results = travelPlanService.getAllTravelPlans();

                // then
                assertThat(results).hasSize(2);
                assertThat(results.get(0).getTitle()).isEqualTo("도쿄 여행");
                assertThat(results.get(1).getTitle()).isEqualTo("파리 여행");

                verify(travelPlanRepository, times(1)).findAllWithDays();
        }

        @Test
        @DisplayName("특정 여행 계획 조회 성공")
        void getTravelPlanById_Success() {
                // given
                Long planId = 1L;
                TravelPlan plan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
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
                verify(travelPlanMapper, times(1)).toDto(plan);
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
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다. ID: " + planId);

                verify(travelPlanRepository, times(1)).findByIdWithDays(planId);
                verify(travelPlanMapper, never()).toDto(any());
        }

        @Test
        @DisplayName("여행 계획 수정 성공")
        void updateTravelPlan_Success() {
                // given
                Long planId = 1L;
                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .title("도쿄 여행 (수정됨)")
                                .participants(3)
                                .build();

                TravelPlan existingPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                TravelPlan updatedPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행 (수정됨)")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(3)
                                .days(new ArrayList<>())
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(planId)
                                .title("도쿄 여행 (수정됨)")
                                .participants(3)
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(updatedPlan);
                when(travelPlanMapper.toDto(updatedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.updateTravelPlan(planId, request);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getTitle()).isEqualTo("도쿄 여행 (수정됨)");
                assertThat(result.getParticipants()).isEqualTo(3);

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, times(1)).save(any(TravelPlan.class));
        }

        @Test
        @DisplayName("존재하지 않는 여행 계획 수정 시 예외 발생")
        void updateTravelPlan_NotFound() {
                // given
                Long planId = 999L;
                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .title("도쿄 여행 (수정됨)")
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> travelPlanService.updateTravelPlan(planId, request))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다. ID: " + planId);

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, never()).save(any());
        }

        @Test
        @DisplayName("여행 계획 삭제 성공 (Soft Delete)")
        void deleteTravelPlan_Success() {
                // given
                Long planId = 1L;
                TravelPlan existingPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
                doNothing().when(travelPlanRepository).delete(existingPlan);

                // when
                travelPlanService.deleteTravelPlan(planId);

                // then
                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, times(1)).delete(existingPlan);
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
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다. ID: " + planId);

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, never()).delete(any());
        }

        @Test
        @DisplayName("여행 계획 수정 - 일차 데이터 전체 교체")
        void updateTravelPlan_ReplaceDays() {
                // given
                Long planId = 1L;

                TravelDayDto newDayDto = TravelDayDto.builder()
                                .dayNumber(1)
                                .date("2024-11-20")
                                .displayDate("11월 20일(수)")
                                .places(new ArrayList<>())
                                .build();

                UpdateTravelPlanRequest request = UpdateTravelPlanRequest.builder()
                                .days(List.of(newDayDto))
                                .build();

                TravelPlan existingPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .days(new ArrayList<>())
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(existingPlan));
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(existingPlan);
                when(travelPlanMapper.toDto(any(TravelPlan.class))).thenReturn(TravelPlanDto.builder().build());

                // when
                travelPlanService.updateTravelPlan(planId, request);

                // then
                ArgumentCaptor<TravelPlan> captor = ArgumentCaptor.forClass(TravelPlan.class);
                verify(travelPlanRepository, times(1)).save(captor.capture());

                TravelPlan capturedPlan = captor.getValue();
                assertThat(capturedPlan.getDays()).hasSize(1);
                assertThat(capturedPlan.getDays().get(0).getDayNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("여행 계획 목록 조회 - 페이징 (필터 없음)")
        void getTravelPlans_WithoutFilter() {
                // given
                TravelPlan plan1 = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .status(TravelPlanStatus.UPCOMING)
                                .isArchived(false)
                                .build();

                TravelPlan plan2 = TravelPlan.builder()
                                .id(2L)
                                .title("파리 여행")
                                .status(TravelPlanStatus.PAST)
                                .isArchived(false)
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder().id(1L).title("도쿄 여행").build();
                TravelPlanDto dto2 = TravelPlanDto.builder().id(2L).title("파리 여행").build();

                Page<TravelPlan> planPage = new PageImpl<>(List.of(plan1, plan2), PageRequest.of(0, 10), 2);

                when(travelPlanRepository.findByIsArchivedFalse(any(PageRequest.class))).thenReturn(planPage);
                when(travelPlanMapper.toDto(plan1)).thenReturn(dto1);
                when(travelPlanMapper.toDto(plan2)).thenReturn(dto2);

                // when
                Page<TravelPlanDto> result = travelPlanService.getTravelPlans(null, 0, 10);

                // then
                assertThat(result.getTotalElements()).isEqualTo(2);
                assertThat(result.getContent()).hasSize(2);
                assertThat(result.getContent().get(0).getTitle()).isEqualTo("도쿄 여행");
                assertThat(result.getContent().get(1).getTitle()).isEqualTo("파리 여행");

                verify(travelPlanRepository, times(1)).findByIsArchivedFalse(any(PageRequest.class));
                verify(travelPlanRepository, never()).findByIsArchivedFalseAndStatus(any(), any());
        }

        @Test
        @DisplayName("여행 계획 목록 조회 - status 필터링 (upcoming)")
        void getTravelPlans_WithStatusFilter() {
                // given
                TravelPlan plan1 = TravelPlan.builder()
                                .id(1L)
                                .title("도쿄 여행")
                                .status(TravelPlanStatus.UPCOMING)
                                .isArchived(false)
                                .build();

                TravelPlanDto dto1 = TravelPlanDto.builder().id(1L).title("도쿄 여행").build();

                Page<TravelPlan> planPage = new PageImpl<>(List.of(plan1), PageRequest.of(0, 10), 1);

                when(travelPlanRepository.findByIsArchivedFalseAndStatus(eq(TravelPlanStatus.UPCOMING),
                                any(PageRequest.class)))
                                .thenReturn(planPage);
                when(travelPlanMapper.toDto(plan1)).thenReturn(dto1);

                // when
                Page<TravelPlanDto> result = travelPlanService.getTravelPlans("upcoming", 0, 10);

                // then
                assertThat(result.getTotalElements()).isEqualTo(1);
                assertThat(result.getContent()).hasSize(1);
                assertThat(result.getContent().get(0).getTitle()).isEqualTo("도쿄 여행");

                verify(travelPlanRepository, times(1))
                                .findByIsArchivedFalseAndStatus(eq(TravelPlanStatus.UPCOMING), any(PageRequest.class));
                verify(travelPlanRepository, never()).findByIsArchivedFalse(any());
        }

        @Test
        @DisplayName("여행 계획 아카이브 성공")
        void archiveTravelPlan_Success() {
                // given
                Long planId = 1L;
                TravelPlan travelPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .isArchived(false)
                                .build();

                TravelPlan archivedPlan = TravelPlan.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .destination("도쿄")
                                .startDate(LocalDate.of(2024, 11, 20))
                                .endDate(LocalDate.of(2024, 11, 23))
                                .participants(2)
                                .isArchived(true)
                                .build();

                TravelPlanDto expectedDto = TravelPlanDto.builder()
                                .id(planId)
                                .title("도쿄 여행")
                                .isArchived(true)
                                .build();

                when(travelPlanRepository.findById(planId)).thenReturn(Optional.of(travelPlan));
                when(travelPlanRepository.save(any(TravelPlan.class))).thenReturn(archivedPlan);
                when(travelPlanMapper.toDto(archivedPlan)).thenReturn(expectedDto);

                // when
                TravelPlanDto result = travelPlanService.archiveTravelPlan(planId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getIsArchived()).isTrue();

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, times(1)).save(any(TravelPlan.class));
                verify(travelPlanMapper, times(1)).toDto(archivedPlan);
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
                                .hasMessageContaining("여행 계획을 찾을 수 없습니다. ID: " + planId);

                verify(travelPlanRepository, times(1)).findById(planId);
                verify(travelPlanRepository, never()).save(any());
        }
}
