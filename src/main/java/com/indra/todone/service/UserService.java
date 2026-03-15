package com.indra.todone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.todone.dto.request.CreateUserRequest;
import com.indra.todone.dto.request.UpdateUserRequest;
import com.indra.todone.dto.response.ProfileResponse;
import com.indra.todone.exception.DuplicatePhoneNumberException;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.model.User;
import com.indra.todone.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private static final String TELEGRAM_WELCOME_MESSAGE = "Welcome! Your account has been linked to this bot. You'll receive task notifications here.";

    private final UserRepository userRepository;
    private final TaskService taskService;
    private final TelegramService telegramService;

    public UserService(UserRepository userRepository, TaskService taskService, TelegramService telegramService) {
        this.userRepository = userRepository;
        this.taskService = taskService;
        this.telegramService = telegramService;
    }

    public User createUser(CreateUserRequest request) {
        userRepository.findFirstByPhoneNumber(request.getPhoneNumber())
                .ifPresent(existing -> {
                    throw new DuplicatePhoneNumberException(request.getPhoneNumber());
                });
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .metadata(request.getMetadata() != null ? request.getMetadata() : Map.of())
                .build();
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getByUserId(String userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getByPhoneNumber(String phoneNumber) {
        return userRepository.findFirstByPhoneNumber(phoneNumber);
    }

    /** Finds user by userId if provided, otherwise by phoneNumber. Returns empty if neither is provided or user not found. */
    public Optional<User> findByIdOrPhoneNumber(String userId, String phoneNumber) {
        if (userId != null && !userId.isBlank()) {
            return userRepository.findById(userId);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            return userRepository.findFirstByPhoneNumber(phoneNumber);
        }
        return Optional.empty();
    }

    public Optional<User> updateByUserId(String userId, UpdateUserRequest request) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getMetadata() != null) {
                        user.setMetadata(request.getMetadata());
                    }
                    return userRepository.save(user);
                });
    }

    public Optional<User> updateByPhoneNumber(String phoneNumber, UpdateUserRequest request) {
        return userRepository.findFirstByPhoneNumber(phoneNumber)
                .map(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName());
                    }
                    if (request.getMetadata() != null) {
                        user.setMetadata(request.getMetadata());
                    }
                    return userRepository.save(user);
                });
    }

    public boolean deleteByUserId(String userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }
        userRepository.deleteById(userId);
        return true;
    }

    public Optional<ProfileResponse> getProfile(String userId) {
        return userRepository.findById(userId)
                .map(user -> {
                    List<Task> tasks = taskService.getTasksForUserId(userId, Optional.empty());
                    long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
                    long pendingCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
                    return ProfileResponse.builder()
                            .user(user)
                            .tasks(tasks)
                            .completedTasksCount(completedCount)
                            .pendingTasksCount(pendingCount)
                            .build();
                });
    }

    /**
     * Links a Telegram bot to the user by fetching getUpdates with the token, extracting the chat id,
     * and saving telegram.token and telegram.chat_id in the user's metadata.
     */
    public Optional<User> linkTelegram(String userId, String telegramToken) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (telegramToken == null || telegramToken.isBlank()) {
                        throw new IllegalArgumentException("telegramToken is required.");
                    }
                    JsonNode updates = telegramService.getUpdates(telegramToken);
                    if (!updates.isArray() || updates.isEmpty()) {
                        throw new IllegalArgumentException("No updates found. Please send a message to the bot first.");
                    }
                    String chatId = null;
                    for (JsonNode update : updates) {
                        JsonNode message = update.path("message");
                        if (message.isMissingNode()) {
                            continue;
                        }
                        JsonNode chat = message.path("chat");
                        if (!chat.isMissingNode() && chat.has("id")) {
                            chatId = chat.get("id").asText();
                            break;
                        }
                    }
                    if (chatId == null) {
                        throw new IllegalArgumentException("No chat id found in updates. Please send a message to the bot first.");
                    }
                    Map<String, Object> metadata = new LinkedHashMap<>(user.getMetadata() != null ? user.getMetadata() : Map.of());
                    metadata.put("telegram", Map.of("token", telegramToken, "chat_id", chatId));
                    user.setMetadata(metadata);
                    User savedUser = userRepository.save(user);
                    try {
                        telegramService.sendMessage(telegramToken, chatId, TELEGRAM_WELCOME_MESSAGE);
                    } catch (Exception e) {
                        log.warn("Failed to send Telegram welcome message to chat {}: {}", chatId, e.getMessage());
                    }
                    return savedUser;
                });
    }
}
