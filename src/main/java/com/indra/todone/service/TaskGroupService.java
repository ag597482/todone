package com.indra.todone.service;

import com.indra.todone.dto.request.CreateTaskGroupRequest;
import com.indra.todone.exception.TaskGroupNotFoundException;
import com.indra.todone.exception.UserNotFoundException;
import com.indra.todone.model.TaskGroup;
import com.indra.todone.model.Task;
import com.indra.todone.repository.TaskGroupRepository;
import com.indra.todone.repository.TaskRepository;
import com.indra.todone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskGroupService {

    private final TaskGroupRepository taskGroupRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<TaskGroup> getAll() {
        return taskGroupRepository.findAll();
    }

    public List<TaskGroup> getAllForUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        return taskGroupRepository.findByAuthorIdOrderByNameAsc(userId);
    }

    public TaskGroup create(CreateTaskGroupRequest request) {
        if (request.getAuthorId() == null || request.getAuthorId().isBlank()) {
            throw new IllegalArgumentException("authorId is required.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("name is required.");
        }
        if (!userRepository.existsById(request.getAuthorId())) {
            throw new UserNotFoundException("User not found for authorId: " + request.getAuthorId());
        }

        TaskGroup group = TaskGroup.builder()
                .taskGroupId(UUID.randomUUID().toString())
                .name(request.getName())
                .authorId(request.getAuthorId())
                .build();
        return taskGroupRepository.save(group);
    }

    public Optional<TaskGroup> updateName(String taskGroupId, String userId, String name) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required.");
        }
        return taskGroupRepository.findById(taskGroupId)
                .map(group -> {
                    if (!group.getAuthorId().equals(userId)) {
                        throw new TaskGroupNotFoundException("Task group not found for this user.");
                    }
                    group.setName(name);
                    return taskGroupRepository.save(group);
                });
    }

    public boolean deleteTaskGroup(String taskGroupId, String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId is required.");
        }
        return taskGroupRepository.findById(taskGroupId)
                .map(group -> {
                    if (!group.getAuthorId().equals(userId)) {
                        throw new TaskGroupNotFoundException("Task group not found for this user.");
                    }
                    // Ungroup tasks owned by the user that reference this group.
                    List<Task> tasks = taskRepository.findByAuthorIdAndTaskGroupId(userId, taskGroupId);
                    for (Task t : tasks) {
                        t.setTaskGroupId(null);
                    }
                    if (!tasks.isEmpty()) {
                        taskRepository.saveAll(tasks);
                    }
                    taskGroupRepository.deleteById(taskGroupId);
                    return true;
                })
                .orElseThrow(() -> new TaskGroupNotFoundException("Task group not found."));
    }
}

