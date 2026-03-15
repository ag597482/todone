package com.indra.todone.controller;

import com.indra.todone.dto.request.GenerateStepsRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.service.OpenAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/openai")
@Tag(name = "OpenAI", description = "Generate actionable steps for tasks using OpenAI")
public class OpenAIController {

    private final OpenAIService openAIService;

    public OpenAIController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/generate-steps")
    @Operation(summary = "Generate steps", description = "Generates short actionable steps for a task from task name and description. Returns at most 4 steps separated as a list.")
    public ResponseEntity<ApiResponse<List<String>>> generateSteps(@RequestBody GenerateStepsRequest request) {
        if (request.getTaskName() == null && request.getTaskDescription() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("taskName and taskDescription cannot both be blank"));
        }
        List<String> steps = openAIService.generateStepsForTask(
                request.getTaskName() != null ? request.getTaskName() : "",
                request.getTaskDescription() != null ? request.getTaskDescription() : ""
        );
        return ResponseEntity.ok(ApiResponse.success(steps));
    }
}
