package org.apples.travelinebackend.mapper;

import org.apples.travelinebackend.dto.TaskDto;
import org.apples.travelinebackend.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        if (task == null) {
            return null;
        }

        return TaskDto.builder()
                .id(task.getId())
                .travelPlanId(task.getTravelPlan().getId())
                .text(task.getText())
                .deadline(task.getDeadline())
                .memo(task.getMemo())
                .checked(task.getChecked())
                .checkedAt(task.getCheckedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}

