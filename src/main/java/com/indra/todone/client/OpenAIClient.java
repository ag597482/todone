package com.indra.todone.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.todone.exception.OpenAIApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Client for OpenAI Responses API (v1/responses).
 * Generates actionable steps from task name and description.
 */
@Slf4j
@Component
public class OpenAIClient {

    private static final String OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
    private static final String SYSTEM_PROMPT = "You generate short actionable steps for completing a task. Always return at most 4 steps can be less depending upon task complexity. give output as strings seperated by ## charachter";

    @Value("${openai.api.token}")
    private String token;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAIClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calls OpenAI Responses API to generate actionable steps for a task.
     *
     * @param taskName        task name
     * @param taskDescription task description
     * @return the raw steps text (steps separated by " ## "), or null if response is incomplete/empty
     */
    public String generateSteps(String taskName, String taskDescription) {
        if (token == null || token.isBlank()) {
            throw new OpenAIApiException("OpenAI API token is not configured. Set OPENAI_API_KEY.");
        }
        String userContent = "Task Name: " + (taskName != null ? taskName : "") + "\nTask Description: " + (taskDescription != null ? taskDescription : "");
        List<Map<String, String>> input = List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", userContent)
        );
        Map<String, Object> body = Map.of(
                "model", model,
                "input", input
        );

        try {
            String bodyJson = objectMapper.writeValueAsString(body);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_RESPONSES_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("OpenAI API Response Status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                String errorMsg = "OpenAI API error: HTTP " + response.statusCode() + " - " + response.body();
                log.error(errorMsg);
                throw new OpenAIApiException(errorMsg);
            }

            return parseOutputText(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("Error calling OpenAI API: {}", e.getMessage());
            throw new OpenAIApiException("OpenAI API request failed: " + e.getMessage(), e);
        }
    }

    private String parseOutputText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String status = root.path("status").asText("");
            if (!"completed".equalsIgnoreCase(status)) {
                log.warn("OpenAI response status is not completed: {}", status);
                return null;
            }
            JsonNode output = root.path("output");
            if (!output.isArray() || output.isEmpty()) {
                log.warn("OpenAI response has no output");
                return null;
            }
            JsonNode firstOutput = output.get(0);
            JsonNode content = firstOutput.path("content");
            if (!content.isArray() || content.isEmpty()) {
                log.warn("OpenAI response output has no content");
                return null;
            }
            JsonNode firstContent = content.get(0);
            JsonNode text = firstContent.path("text");
            if (text.isMissingNode() || !text.isTextual()) {
                return null;
            }
            return text.asText();
        } catch (IOException e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            throw new OpenAIApiException("Invalid response format from OpenAI API", e);
        }
    }
}
