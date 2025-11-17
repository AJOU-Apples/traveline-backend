package org.apples.travelinebackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apples.travelinebackend.dto.*;
import org.apples.travelinebackend.entity.User;
import org.apples.travelinebackend.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/travel-plans/{planId}/tasks")
    public ResponseEntity<TaskDto> createTask(
            @PathVariable Long planId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal User user) {
        log.info("POST /api/travel-plans/{}/tasks - 작업 생성 요청", planId);
        request.setTravelPlanId(planId);
        TaskDto task = taskService.createTask(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @GetMapping("/travel-plans/{planId}/tasks")
    public ResponseEntity<List<TaskDto>> getTasks(
            @PathVariable Long planId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/tasks - 작업 목록 조회", planId);
        List<TaskDto> tasks = taskService.getTasksByTravelPlan(planId, user);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal User user) {
        log.info("PATCH /api/tasks/{} - 작업 수정 요청", taskId);
        TaskDto task = taskService.updateTask(taskId, request, user);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/tasks/{} - 작업 삭제 요청", taskId);
        taskService.deleteTask(taskId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/travel-plans/{planId}/tasks/summary")
    public ResponseEntity<TaskSummaryDto> getTaskSummary(
            @PathVariable Long planId,
            @AuthenticationPrincipal User user) {
        log.info("GET /api/travel-plans/{}/tasks/summary - 작업 요약 조회", planId);
        TaskSummaryDto summary = taskService.getTaskSummary(planId, user);
        return ResponseEntity.ok(summary);
    }
}

