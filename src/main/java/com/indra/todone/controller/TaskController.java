package com.indra.todone.controller;

import com.indra.todone.dto.request.CreateTaskRequest;
import com.indra.todone.dto.request.UpdateTaskStatusRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.model.Task;
import com.indra.todone.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task", description = "Task management APIs")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create task", description = "Creates a new task. Returns the created task.")
    public ResponseEntity<ApiResponse<Task>> createTask(@RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Task created successfully", task));
    }

    @GetMapping
    @Operation(summary = "Get all tasks", description = "Returns all tasks.")
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        return ResponseEntity.ok(ApiResponse.success(taskService.getAllTasks()));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID", description = "Returns a single task by task_id. 404 if not found.")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable String taskId) {
        return taskService.getByTaskId(taskId)
                .map(task -> ResponseEntity.ok(ApiResponse.success(task)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Task not found")));
    }

    @PutMapping("/{taskId}/status")
    @Operation(summary = "Update task status", description = "Updates the task status. Body: taskStatus, userId. Only the author can update. 403 if not author, 404 if task not found.")
    public ResponseEntity<ApiResponse<Task>> updateTaskStatus(
            @PathVariable String taskId,
            @RequestBody UpdateTaskStatusRequest request) {
        Optional<Task> updated = taskService.updateStatus(taskId, request);
        return updated
                .map(task -> ResponseEntity.ok(ApiResponse.success("Status updated", task)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Task not found")));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tasks for user", description = "Returns tasks for the given user. Optional query param date (yyyy-MM-dd): if present, tasks with dueDate on that date; otherwise all tasks for the user.")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksForUser(
            @PathVariable String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Task> tasks = taskService.getTasksForUserId(userId, Optional.ofNullable(date));
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task", description = "Deletes the task. Query param userId required. Only the author can delete. 403 if not author, 404 if task not found.")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable String taskId,
            @RequestParam String userId) {
        if (taskService.deleteByTaskIdAndUserId(taskId, userId)) {
            return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Task not found"));
    }
}
