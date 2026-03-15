package com.indra.todone.service;

import com.indra.todone.client.OpenAIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private static final String STEP_SEPARATOR = " ## ";

    private final OpenAIClient openAIClient;

    /**
     * Generates actionable steps for a task using OpenAI. Returns steps as a list of strings.
     */
    public List<String> generateStepsForTask(String taskName, String taskDescription) {
        String raw = openAIClient.generateSteps(taskName, taskDescription);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(STEP_SEPARATOR))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
