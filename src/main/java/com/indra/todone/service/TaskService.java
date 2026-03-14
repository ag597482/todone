package com.indra.todone.service;

import com.indra.todone.dto.request.CreateTaskRequest;
import com.indra.todone.dto.request.UpdateTaskStatusRequest;
import com.indra.todone.exception.UnauthorizedTaskAccessException;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public Task createTask(CreateTaskRequest request) {
        Task task = Task.builder()
                .taskId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .meta(request.getMeta() != null ? request.getMeta() : Map.of())
                .dueDate(request.getDueDate())
                .doneDate(null)
                .status(TaskStatus.PENDING)
                .authorId(request.getAuthorId())
                .build();
        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getByTaskId(String taskId) {
        return taskRepository.findById(taskId);
    }

    public Optional<Task> updateStatus(String taskId, UpdateTaskStatusRequest request) {
        Optional<Task> opt = taskRepository.findById(taskId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Task task = opt.get();
        if (!task.getAuthorId().equals(request.getUserId())) {
            throw new UnauthorizedTaskAccessException("Only the task author can perform this action.");
        }
        task.setStatus(request.getTaskStatus());
        if (request.getTaskStatus() == TaskStatus.COMPLETED) {
            task.setDoneDate(LocalDate.now());
        } else {
            task.setDoneDate(null);
        }
        return Optional.of(taskRepository.save(task));
    }

    public List<Task> getTasksForUserId(String userId, Optional<LocalDate> date) {
        if (date.isPresent()) {
            return taskRepository.findByAuthorIdAndDueDate(userId, date.get());
        }
        return taskRepository.findByAuthorId(userId);
    }

    public boolean deleteByTaskIdAndUserId(String taskId, String userId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isEmpty()) {
            return false;
        }
        if (!task.get().getAuthorId().equals(userId)) {
            throw new UnauthorizedTaskAccessException("Only the task author can delete this task.");
        }
        taskRepository.delete(task.get());
        return true;
    }
}
