package com.indra.todone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.todone.client.OpenAIClient;
import com.indra.todone.dto.response.NotificationBodyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpenAIService {

    private static final String STEP_SEPARATOR = " ## ";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    /**
     * Generates a notification title and body from a JSON string of tasks using OpenAI.
     */
    public NotificationBodyResponse generateNotificationBody(String tasksJson) {
        String raw = openAIClient.generateNotificationBody(tasksJson != null ? tasksJson : "[]");
        if (raw == null || raw.isBlank()) {
            return NotificationBodyResponse.builder()
                    .title("Tasks reminder")
                    .body("You have pending tasks to complete.")
                    .build();
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(raw.trim());
            String title = node.has("title") ? node.get("title").asText("Tasks reminder") : "Tasks reminder";
            String body = node.has("body") ? node.get("body").asText("") : "You have pending tasks to complete.";
            return NotificationBodyResponse.builder().title(title).body(body).build();
        } catch (Exception e) {
            return NotificationBodyResponse.builder()
                    .title("Tasks reminder")
                    .body(raw)
                    .build();
        }
    }
}
