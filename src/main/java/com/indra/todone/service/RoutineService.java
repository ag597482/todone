package com.indra.todone.service;

import com.indra.todone.dto.request.CreateRoutineRequest;
import com.indra.todone.model.Routine;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.repository.RoutineRepository;
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
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final TaskRepository taskRepository;

    public Routine createRoutine(CreateRoutineRequest request) {
        Routine routine = Routine.builder()
                .routineId(UUID.randomUUID().toString())
                .name(request.getName())
                .desc(request.getDesc())
                .meta(request.getMeta() != null ? request.getMeta() : Map.of())
                .repeatFrequencyDays(request.getRepeatFrequencyDays())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isExecuted(false)
                .authorId(request.getAuthorId())
                .build();
        return routineRepository.save(routine);
    }

    public List<Routine> getAllRoutines() {
        return routineRepository.findAll();
    }

    public List<Routine> getRoutinesForUserId(String userId, boolean isActive, Optional<Boolean> isExecuted) {
        List<Routine> routines = routineRepository.findByAuthorId(userId);
        if (isActive) {
            LocalDate today = LocalDate.now();
            routines = routines.stream()
                    .filter(r -> !today.isBefore(r.getStartDate()) && !today.isAfter(r.getEndDate()))
                    .toList();
        }
        if (isExecuted.isPresent()) {
            boolean executed = isExecuted.get();
            routines = routines.stream()
                    .filter(r -> r.isExecuted() == executed)
                    .toList();
        }
        return routines;
    }

    public Optional<Routine> executeRoutine(String routineId) {
        Optional<Routine> opt = routineRepository.findById(routineId);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        Routine routine = opt.get();
        LocalDate start = routine.getStartDate();
        LocalDate end = routine.getEndDate();
        int step = Math.max(1, routine.getRepeatFrequencyDays());
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(step)) {
            Task task = Task.builder()
                    .taskId(UUID.randomUUID().toString())
                    .name(routine.getName())
                    .description(routine.getDesc())
                    .meta(routine.getMeta() != null ? routine.getMeta() : Map.of())
                    .dueDate(d)
                    .doneDate(null)
                    .status(TaskStatus.PENDING)
                    .authorId(routine.getAuthorId())
                    .build();
            taskRepository.save(task);
        }
        routine.setExecuted(true);
        return Optional.of(routineRepository.save(routine));
    }
}
