package com.indra.todone.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indra.todone.dto.response.TwoFactorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for interacting with 2Factor.in API.
 * Handles HTTP communication and API response parsing.
 */
@Slf4j
@Component
public class TwoFactorClient {

    private static final String BASE_URL = "https://2factor.in/API/V1";

    @Value("${twofactor.api.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public TwoFactorClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sends OTP via 2Factor API.
     *
     * @param phoneNumber formatted phone number with country code
     * @param templateName SMS template name
     * @return TwoFactorResponse containing session ID and OTP details
     */
    public TwoFactorResponse sendOTP(String phoneNumber, String templateName) throws Exception {
        try {
            String url = String.format("%s/%s/SMS/%s/AUTOGEN3/%s",
                    BASE_URL, apiKey, phoneNumber, templateName);

            log.debug("Sending OTP request to URL: {}", url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            log.info("2Factor API Response Status: {}", response.statusCode());
            log.debug("2Factor API Response Body: {}", response.body());

            return parseResponse(response, "send OTP");

        } catch (IOException | InterruptedException e) {
            log.error("Error calling 2Factor API for OTP send: {}", e.getMessage());
            throw new Exception("Failed to send OTP via 2Factor API: " + e.getMessage(), e);
        }
    }

    /**
     * Verifies OTP via 2Factor API.
     *
     * @param sessionId  session ID from OTP send response
     * @param enteredOTP OTP entered by user
     * @return TwoFactorResponse containing verification result
     */
    public TwoFactorResponse verifyOTP(String sessionId, String enteredOTP) throws Exception {
        try {
            String url = String.format("%s/%s/SMS/VERIFY/%s/%s",
                    BASE_URL, apiKey, sessionId, enteredOTP);

            log.info("Verifying OTP for session: {}", sessionId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            log.info("OTP Verification Response Status: {}", response.statusCode());
            log.debug("OTP Verification Response Body: {}", response.body());

            return parseResponse(response, "verify OTP");

        } catch (IOException | InterruptedException e) {
            log.error("Error calling 2Factor API for OTP verification: {}", e.getMessage());
            throw new Exception("Failed to verify OTP via 2Factor API: " + e.getMessage(), e);
        }
    }

    private TwoFactorResponse parseResponse(HttpResponse<String> response, String operation) throws Exception {
        String body = response.body();
        if (body == null || body.isBlank()) {
            log.warn("2Factor API returned empty body for {} (HTTP {})", operation, response.statusCode());
            return TwoFactorResponse.builder()
                    .status("Error")
                    .details("Empty response from 2Factor API")
                    .build();
        }
        if (response.statusCode() != 200) {
            String errorMsg = String.format("2Factor API error for %s: HTTP %d - %s",
                    operation, response.statusCode(), body);
            log.error(errorMsg);
            throw new Exception(errorMsg);
        }
        try {
            return objectMapper.readValue(body, TwoFactorResponse.class);
        } catch (IOException e) {
            log.error("Failed to parse 2Factor API response for {}: {} - body: {}", operation, e.getMessage(), body);
            throw new Exception("Invalid response format from 2Factor API", e);
        }
    }
}
