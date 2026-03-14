package com.indra.todone.controller;

import com.indra.todone.dto.request.CreateRoutineRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.model.Routine;
import com.indra.todone.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routines")
@Tag(name = "Routine", description = "Routine management APIs")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping
    @Operation(summary = "Create routine", description = "Creates a new routine. Returns the created routine.")
    public ResponseEntity<ApiResponse<Routine>> createRoutine(@RequestBody CreateRoutineRequest request) {
        Routine routine = routineService.createRoutine(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Routine created successfully", routine));
    }

    @GetMapping
    @Operation(summary = "Get all routines", description = "Returns all routines.")
    public ResponseEntity<ApiResponse<List<Routine>>> getAllRoutines() {
        return ResponseEntity.ok(ApiResponse.success(routineService.getAllRoutines()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get routines for user", description = "Returns routines for the given user. isActive (default true): when true, only routines where current date is between startDate and endDate. isExecuted (optional): when provided, filter by executed state (true/false); when omitted, return all regardless of executed state.")
    public ResponseEntity<ApiResponse<List<Routine>>> getRoutinesForUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "true") boolean isActive,
            @RequestParam(required = false) Boolean isExecuted) {
        List<Routine> routines = routineService.getRoutinesForUserId(userId, isActive, Optional.ofNullable(isExecuted));
        return ResponseEntity.ok(ApiResponse.success(routines));
    }

    @PostMapping("/{routineId}/execute")
    @Operation(summary = "Execute routine", description = "Marks routine as executed and generates tasks in the tasks collection for the date range [startDate, endDate] every repeatFrequencyDays. Returns the updated routine. 404 if routine not found.")
    public ResponseEntity<ApiResponse<Routine>> executeRoutine(@PathVariable String routineId) {
        Optional<Routine> routine = routineService.executeRoutine(routineId);
        return routine
                .map(r -> ResponseEntity.ok(ApiResponse.success("Routine executed, tasks generated", r)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Routine not found")));
    }
}
