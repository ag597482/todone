package com.indra.todone.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.todone.exception.TelegramApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Client for Telegram Bot API (sendMessage, getUpdates).
 * Token is passed per-request to support multiple bots.
 */
@Slf4j
@Component
public class TelegramClient {

    private static final String BASE_URL = "https://api.telegram.org/bot%s";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TelegramClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends a text message to a chat via Telegram Bot API.
     *
     * @param botToken bot token
     * @param chatId  chat ID
     * @param text    message text
     * @return the result object (message_id, from, chat, date, text) as JsonNode
     */
    public JsonNode sendMessage(String botToken, String chatId, String text) {
        String baseUrl = String.format(BASE_URL, botToken);
        String body = "chat_id=" + URLEncoder.encode(chatId != null ? chatId : "", StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text != null ? text : "", StandardCharsets.UTF_8);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/sendMessage"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseTelegramResponse(response.statusCode(), response.body(), baseUrl + "/sendMessage");
        } catch (IOException | InterruptedException e) {
            log.error("Telegram sendMessage failed: {}", e.getMessage());
            throw new TelegramApiException("Telegram API request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetches updates (messages, etc.) for the bot.
     *
     * @param botToken bot token
     * @return the result array of updates as JsonNode
     */
    public JsonNode getUpdates(String botToken) {
        String baseUrl = String.format(BASE_URL, botToken);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/getUpdates"))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseTelegramResponse(response.statusCode(), response.body(), baseUrl + "/getUpdates");
        } catch (IOException | InterruptedException e) {
            log.error("Telegram getUpdates failed: {}", e.getMessage());
            throw new TelegramApiException("Telegram API request failed: " + e.getMessage(), e);
        }
    }

    private JsonNode parseTelegramResponse(int statusCode, String body, String endpoint) {
        try {
            JsonNode root = objectMapper.readTree(body);
            boolean ok = root.path("ok").asBoolean(false);
            if (statusCode != 200) {
                String desc = root.path("description").asText("Unknown error");
                throw new TelegramApiException("Telegram API error: HTTP " + statusCode + " - " + desc);
            }
            if (!ok) {
                String desc = root.path("description").asText("Unknown error");
                throw new TelegramApiException("Telegram API error: " + desc);
            }
            return root.path("result");
        } catch (IOException e) {
            log.error("Failed to parse Telegram response: {}", e.getMessage());
            throw new TelegramApiException("Invalid response format from Telegram API", e);
        }
    }
}
