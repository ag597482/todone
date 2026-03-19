package com.indra.todone.controller;

import com.indra.todone.dto.request.CreateTaskGroupRequest;
import com.indra.todone.dto.request.UpdateTaskGroupRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.model.TaskGroup;
import com.indra.todone.service.TaskGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-groups")
@Tag(name = "Task Group", description = "Task group management APIs")
public class TaskGroupController {

    private final TaskGroupService taskGroupService;

    public TaskGroupController(TaskGroupService taskGroupService) {
        this.taskGroupService = taskGroupService;
    }

    @GetMapping
    @Operation(summary = "List task groups for user", description = "Returns all task groups created by the user, ordered by name. Query param: userId.")
    public ResponseEntity<ApiResponse<List<TaskGroup>>> getTaskGroupsForUser(@RequestParam String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("userId is required."));
        }
        List<TaskGroup> groups = taskGroupService.getAllForUser(userId);
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all task groups", description = "Returns all existing task groups (for all users).")
    public ResponseEntity<ApiResponse<List<TaskGroup>>> getAllTaskGroups() {
        return ResponseEntity.ok(ApiResponse.success(taskGroupService.getAll()));
    }

    @PostMapping
    @Operation(summary = "Create task group", description = "Creates a new task group. Body: name, authorId. Returns the created task group.")
    public ResponseEntity<ApiResponse<TaskGroup>> createTaskGroup(@RequestBody CreateTaskGroupRequest request) {
        TaskGroup group = taskGroupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Task group created", group));
    }

    @PutMapping("/{taskGroupId}")
    @Operation(summary = "Update task group name", description = "Updates a task group's name. Query param userId required. Only the author can update.")
    public ResponseEntity<ApiResponse<TaskGroup>> updateTaskGroupName(
            @PathVariable String taskGroupId,
            @RequestParam String userId,
            @RequestBody UpdateTaskGroupRequest request) {
        return taskGroupService.updateName(taskGroupId, userId, request.getName())
                .map(group -> ResponseEntity.ok(ApiResponse.success("Task group updated", group)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Task group not found")));
    }

    @DeleteMapping("/{taskGroupId}")
    @Operation(summary = "Delete task group", description = "Deletes a task group. Query param userId required. Ungroups tasks that belong to the user and reference this group.")
    public ResponseEntity<ApiResponse<Void>> deleteTaskGroup(
            @PathVariable String taskGroupId,
            @RequestParam String userId) {
        taskGroupService.deleteTaskGroup(taskGroupId, userId);
        return ResponseEntity.ok(ApiResponse.success("Task group deleted successfully"));
    }
}

