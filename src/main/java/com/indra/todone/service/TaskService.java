package com.indra.todone.service;

import com.indra.todone.dto.request.CreateTaskRequest;
import com.indra.todone.dto.request.UpdateSubtaskStatusRequest;
import com.indra.todone.dto.request.UpdateTaskStatusRequest;
import com.indra.todone.dto.request.UpdateTaskRequest;
import com.indra.todone.exception.SubtaskNotFoundException;
import com.indra.todone.exception.UnauthorizedTaskAccessException;
import com.indra.todone.exception.UserNotFoundException;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStep;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.repository.TaskRepository;
import com.indra.todone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Task createTask(CreateTaskRequest request) {
        String authorId = request.getAuthorId();
        if (authorId == null || authorId.isBlank()) {
            throw new IllegalArgumentException("authorId is required.");
        }
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("User not found for authorId: " + authorId);
        }
        Map<String, Object> meta = request.getMeta() != null
                ? new LinkedHashMap<>(request.getMeta())
                : new LinkedHashMap<>();
        if (request.getTime() != null) {
            meta.put("time", request.getTime());
        }

        Task task = Task.builder()
                .taskId(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .meta(meta)
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

    public Optional<Task> updateSubtaskStatus(String taskId, UpdateSubtaskStatusRequest request) {
        Optional<Task> opt = taskRepository.findById(taskId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Task task = opt.get();
        if (!task.getAuthorId().equals(request.getUserId())) {
            throw new UnauthorizedTaskAccessException("Only the task author can perform this action.");
        }
        Map<String, Object> meta = task.getMeta();
        if (meta == null) {
            throw new SubtaskNotFoundException("Task has no steps.");
        }
        Object stepsObj = meta.get("steps");
        if (!(stepsObj instanceof List)) {
            throw new SubtaskNotFoundException("Task has no steps.");
        }
        List<?> stepsList = (List<?>) stepsObj;
        if (stepsList.isEmpty()) {
            throw new SubtaskNotFoundException("Subtask not found.");
        }
        String targetValue = normalizeStepValue(request.getSubtaskValue() != null ? request.getSubtaskValue() : "");
        boolean found = false;
        for (int i = 0; i < stepsList.size(); i++) {
            Object step = stepsList.get(i);
            String stepValue = getStepValue(step);
            if (stepValue == null) {
                continue;
            }
            if (normalizeStepValue(stepValue).equals(targetValue)) {
                setStepCompleted(step, i, stepsList, request.isCompleted());
                found = true;
                break;
            }
        }
        if (!found) {
            throw new SubtaskNotFoundException("Subtask not found.");
        }
        return Optional.of(taskRepository.save(task));
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
            markAllSubtasksCompleted(task);
        } else {
            task.setDoneDate(null);
        }
        return Optional.of(taskRepository.save(task));
    }

    public Optional<Task> updateTask(String taskId, UpdateTaskRequest request) {
        Optional<Task> opt = taskRepository.findById(taskId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Task task = opt.get();
        if (!task.getAuthorId().equals(request.getUserId())) {
            throw new UnauthorizedTaskAccessException("Only the task author can perform this action.");
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            task.setName(request.getName());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        List<TaskStep> newSteps = request.getSteps();
        if (newSteps == null && request.getMeta() != null) {
            Object stepsObj = request.getMeta().get("steps");
            if (stepsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> stepMaps = (List<Map<String, Object>>) stepsObj;
                List<TaskStep> converted = new ArrayList<>();
                for (Map<String, Object> s : stepMaps) {
                    Object value = s.get("value");
                    Object completedObj = s.get("completed");
                    boolean completed = completedObj instanceof Boolean && (Boolean) completedObj;
                    converted.add(TaskStep.builder()
                            .value(value != null ? value.toString() : null)
                            .completed(completed)
                            .build());
                }
                newSteps = converted;
            }
        }

        Map<String, Object> meta = task.getMeta() != null
                ? new LinkedHashMap<>(task.getMeta())
                : new LinkedHashMap<>();
        if (newSteps != null) {
            meta.put("steps", new ArrayList<>(newSteps));
        }
        if (request.getTime() != null) {
            meta.put("time", request.getTime());
        } else if (request.getMeta() != null && request.getMeta().get("time") != null) {
            meta.put("time", request.getMeta().get("time"));
        }
        if (newSteps != null || request.getTime() != null || (request.getMeta() != null && request.getMeta().get("time") != null)) {
            task.setMeta(meta);
        }

        return Optional.of(taskRepository.save(task));
    }

    /** Sets every step in meta.steps to completed = true. */
    private void markAllSubtasksCompleted(Task task) {
        Map<String, Object> meta = task.getMeta();
        if (meta == null) {
            return;
        }
        Object stepsObj = meta.get("steps");
        if (!(stepsObj instanceof List)) {
            return;
        }
        List<?> stepsList = (List<?>) stepsObj;
        for (int i = 0; i < stepsList.size(); i++) {
            setStepCompleted(stepsList.get(i), i, stepsList, true);
        }
    }

    public List<Task> getTasksForUserId(String userId, Optional<LocalDate> date) {
        if (date.isPresent()) {
            LocalDate inputDate = date.get();
            LocalDate today = LocalDate.now();
            List<Task> pendingByDate = taskRepository.findByAuthorIdAndStatusAndDueDateLessThanEqual(userId, TaskStatus.PENDING, inputDate);
            List<Task> completedByDate = taskRepository.findByAuthorIdAndStatusAndDueDateLessThanEqual(userId, TaskStatus.COMPLETED, inputDate);
            List<Task> merged = mergeTasksById(pendingByDate, completedByDate);
            if (inputDate.isBefore(today)) {
                List<Task> todayPending = taskRepository.findByAuthorIdAndStatusAndDueDate(userId, TaskStatus.PENDING, today);
                return mergeTasksById(merged, todayPending);
            }
            return merged;
        }
        return taskRepository.findByAuthorId(userId);
    }

    private static List<Task> mergeTasksById(List<Task> list1, List<Task> list2) {
        Map<String, Task> byId = new LinkedHashMap<>();
        for (Task t : list1) {
            byId.put(t.getTaskId(), t);
        }
        for (Task t : list2) {
            byId.putIfAbsent(t.getTaskId(), t);
        }
        return new ArrayList<>(byId.values());
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

    /** Trim and normalize whitespace for reliable step value matching. */
    private static String normalizeStepValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    /** Extract the step text from a step object (Map, TaskStep, String, or BSON-friendly Map). */
    private static String getStepValue(Object step) {
        if (step == null) {
            return null;
        }
        if (step instanceof TaskStep) {
            return ((TaskStep) step).getValue();
        }
        if (step instanceof String) {
            return (String) step;
        }
        if (step instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) step;
            Object v = m.get("value");
            if (v == null) {
                v = m.get("Value");
            }
            if (v == null) {
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    if (e.getKey() != null && "value".equalsIgnoreCase(e.getKey())) {
                        v = e.getValue();
                        break;
                    }
                }
            }
            return v != null ? v.toString() : null;
        }
        return null;
    }

    /** Set completed on the step and ensure it is a mutable map so it persists (for Map/BSON steps). */
    private void setStepCompleted(Object step, int index, List<?> stepsList, boolean completed) {
        if (step instanceof TaskStep) {
            ((TaskStep) step).setCompleted(completed);
            return;
        }
        if (step instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> stepMap = (Map<String, Object>) step;
            stepMap.put("completed", completed);
            return;
        }
        if (step instanceof String) {
            Map<String, Object> newStep = new LinkedHashMap<>();
            newStep.put("value", step);
            newStep.put("completed", completed);
            @SuppressWarnings("unchecked")
            List<Object> mutableSteps = (List<Object>) stepsList;
            mutableSteps.set(index, newStep);
        }
    }
}
