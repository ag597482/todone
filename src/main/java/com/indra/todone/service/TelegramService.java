package com.indra.todone.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.indra.todone.client.TelegramClient;
import com.indra.todone.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TelegramService {

    private static final String METADATA_KEY_TELEGRAM = "telegram";
    private static final String TELEGRAM_KEY_TOKEN = "token";
    private static final String TELEGRAM_KEY_CHAT_ID = "chat_id";

    private final TelegramClient telegramClient;

    /**
     * Sends a text message to a Telegram chat. Validates inputs and delegates to the client.
     */
    public JsonNode sendMessage(String botToken, String chatId, String text) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalArgumentException("botToken is required.");
        }
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("chatId is required.");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text is required.");
        }
        return telegramClient.sendMessage(botToken, chatId, text);
    }

    /**
     * Fetches updates for the bot. Validates token and delegates to the client.
     */
    public JsonNode getUpdates(String botToken) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalArgumentException("token is required.");
        }
        return telegramClient.getUpdates(botToken);
    }

    /**
     * Sends a text message to the user's linked Telegram chat if present in user metadata.
     * Expects user.getMetadata().telegram to contain "token" and "chat_id".
     */
    public JsonNode sendMessageToUser(User user, String text) {
        if (user == null || user.getMetadata() == null) {
            throw new IllegalArgumentException("User has not linked Telegram.");
        }
        Object telegramObj = user.getMetadata().get(METADATA_KEY_TELEGRAM);
        if (!(telegramObj instanceof Map)) {
            throw new IllegalArgumentException("User has not linked Telegram.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> telegram = (Map<String, Object>) telegramObj;
        Object tokenObj = telegram.get(TELEGRAM_KEY_TOKEN);
        Object chatIdObj = telegram.get(TELEGRAM_KEY_CHAT_ID);
        String token = tokenObj != null ? tokenObj.toString() : null;
        String chatId = chatIdObj != null ? chatIdObj.toString() : null;
        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("User has not linked Telegram.");
        }
        return sendMessage(token, chatId, text != null ? text : "");
    }
}
