package org.apples.travelinebackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.Task;
import org.apples.travelinebackend.entity.TravelPlan;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.exception.ForbiddenException;
import org.apples.travelinebackend.exception.ResourceNotFoundException;
import org.apples.travelinebackend.mapper.TaskMapper;
import org.apples.travelinebackend.repository.TaskRepository;
import org.apples.travelinebackend.repository.TravelPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TravelPlanRepository travelPlanRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskDto createTask(CreateTaskRequest request, User user) {
        log.info("작업 생성 요청: travelPlanId={}, userId={}", request.getTravelPlanId(), user.getId());

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(request.getTravelPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + request.getTravelPlanId()));

        if (!travelPlan.hasRole(user.getId(), org.apples.travelinebackend.entity.MemberRole.EDITOR)) {
            throw new ForbiddenException("작업을 등록할 권한이 없습니다");
        }

        Task task = Task.builder()
                .travelPlan(travelPlan)
                .text(request.getText())
                .deadline(request.getDeadline())
                .memo(request.getMemo())
                .checked(false)
                .build();

        Task saved = taskRepository.save(task);
        log.info("작업 생성 완료: taskId={}", saved.getId());

        return taskMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByTravelPlan(Long travelPlanId, User user) {
        log.info("여행 계획별 작업 목록 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.hasAccess(user.getId())) {
            throw new ForbiddenException("여행 계획에 접근할 권한이 없습니다");
        }

        List<Task> tasks = taskRepository.findByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        return tasks.stream()
                .map(taskMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDto updateTask(Long taskId, UpdateTaskRequest request, User user) {
        log.info("작업 수정 요청: taskId={}, userId={}", taskId, user.getId());

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("작업을 찾을 수 없습니다: " + taskId));

        if (!task.getTravelPlan().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 작업만 수정할 수 있습니다");
        }

        // 부분 업데이트
        if (request.getText() != null) task.setText(request.getText());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());
        if (request.getMemo() != null) task.setMemo(request.getMemo());
        
        if (request.getChecked() != null) {
            task.setChecked(request.getChecked());
            task.setCheckedAt(request.getChecked() ? LocalDateTime.now() : null);
        }

        Task updated = taskRepository.save(task);
        log.info("작업 수정 완료: taskId={}", taskId);

        return taskMapper.toDto(updated);
    }

    @Transactional
    public void deleteTask(Long taskId, User user) {
        log.info("작업 삭제 요청: taskId={}, userId={}", taskId, user.getId());

        Task task = taskRepository.findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("작업을 찾을 수 없습니다: " + taskId));

        if (!task.getTravelPlan().getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 작업만 삭제할 수 있습니다");
        }

        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);

        log.info("작업 삭제 완료: taskId={}", taskId);
    }

    @Transactional(readOnly = true)
    public TaskSummaryDto getTaskSummary(Long travelPlanId, User user) {
        log.info("작업 요약 조회: travelPlanId={}", travelPlanId);

        // TravelPlan 존재 및 권한 확인
        TravelPlan travelPlan = travelPlanRepository.findByIdWithMembers(travelPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("여행 계획을 찾을 수 없습니다: " + travelPlanId));

        if (!travelPlan.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 여행 계획만 조회할 수 있습니다");
        }

        int total = taskRepository.countByTravelPlanIdAndDeletedAtIsNull(travelPlanId);
        int checked = taskRepository.countCheckedByTravelPlanId(travelPlanId);

        return TaskSummaryDto.builder()
                .total(total)
                .checked(checked)
                .build();
    }
}

