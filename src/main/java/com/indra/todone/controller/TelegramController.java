package com.indra.todone.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.todone.dto.request.SendTelegramMessageRequest;
import com.indra.todone.dto.request.SendTelegramToUserRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.model.User;
import com.indra.todone.service.TelegramService;
import com.indra.todone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/telegram")
@Tag(name = "Telegram", description = "Telegram Bot API: send message and get updates")
public class TelegramController {

    private final TelegramService telegramService;
    private final UserService userService;

    public TelegramController(TelegramService telegramService, UserService userService) {
        this.telegramService = telegramService;
        this.userService = userService;
    }

    @PostMapping("/send-message")
    @Operation(summary = "Send message", description = "Sends a text message to a Telegram chat. Body: botToken, chatId, text.")
    public ResponseEntity<ApiResponse<JsonNode>> sendMessage(@RequestBody SendTelegramMessageRequest request) {
        if (request.getBotToken() == null || request.getBotToken().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("botToken is required."));
        }
        if (request.getChatId() == null || request.getChatId().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("chatId is required."));
        }
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text is required."));
        }
        JsonNode result = telegramService.sendMessage(request.getBotToken(), request.getChatId(), request.getText());
        return ResponseEntity.ok(ApiResponse.success("Message sent", result));
    }

    @PostMapping("/send-to-user")
    @Operation(summary = "Send message to user", description = "Sends a text message to the user's linked Telegram. Provide either userId or phoneNumber; if the user has Telegram in metadata (token and chat_id), the message is sent. 404 if user not found, 400 if Telegram not linked or text missing.")
    public ResponseEntity<ApiResponse<JsonNode>> sendToUser(@RequestBody SendTelegramToUserRequest request) {
        boolean hasUserId = request.getUserId() != null && !request.getUserId().isBlank();
        boolean hasPhone = request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank();
        if (!hasUserId && !hasPhone) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Either userId or phoneNumber is required."));
        }
        if (hasUserId && hasPhone) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Provide only one of userId or phoneNumber."));
        }
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("text is required."));
        }
        Optional<User> userOpt = userService.findByIdOrPhoneNumber(request.getUserId(), request.getPhoneNumber());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("User not found."));
        }
        try {
            JsonNode result = telegramService.sendMessageToUser(userOpt.get(), request.getText());
            return ResponseEntity.ok(ApiResponse.success("Message sent", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/updates")
    @Operation(summary = "Get updates", description = "Fetches updates (messages, etc.) for the bot. Query param: token (bot token).")
    public ResponseEntity<ApiResponse<JsonNode>> getUpdates(@RequestParam(name = "token") String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("token is required."));
        }
        JsonNode result = telegramService.getUpdates(token);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
