package com.indra.todone.service;

import com.indra.todone.client.TwoFactorClient;
import com.indra.todone.dto.request.SendOtpRequest;
import com.indra.todone.dto.request.VerifyOtpRequest;
import com.indra.todone.dto.response.SendOtpResponse;
import com.indra.todone.dto.response.TwoFactorResponse;
import com.indra.todone.dto.response.VerifyOtpResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmsService {

    private static final String MOCK_OTP = "0000";
    private static final String SUCCESS_STATUS = "Success";

    private final TwoFactorClient twoFactorClient;

    @Value("${twofactor.otpMock:true}")
    private boolean otpMock;

    @Value("${twofactor.templateName:inqotp}")
    private String templateName;

    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String phoneNumber = request.getPhoneNumber();

        if (otpMock) {
            log.info("OTP Mock enabled: skipping 2Factor API call for phone {}", phoneNumber);
            String mockSessionId = "MOCK-SESSION-" + UUID.randomUUID();
            return SendOtpResponse.builder()
                    .sessionId(mockSessionId)
                    .message("OTP sent (mock). Use " + MOCK_OTP + " to verify.")
                    .build();
        }

        try {
            TwoFactorResponse response = twoFactorClient.sendOTP(phoneNumber, templateName);
            if (SUCCESS_STATUS.equalsIgnoreCase(response.getStatus())) {
                return SendOtpResponse.builder()
                        .sessionId(response.getDetails())
                        .message("OTP sent successfully")
                        .build();
            }
            throw new RuntimeException("2Factor API returned: " + response.getStatus() + " - " + response.getDetails());
        } catch (Exception e) {
            log.error("Failed to send OTP: {}", e.getMessage());
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        String sessionId = request.getSessionId();
        String otp = request.getOtp();

        if (otpMock) {
            log.info("OTP Mock enabled: verifying without 2Factor API. Session={}", sessionId);
            boolean verified = MOCK_OTP.equals(otp);
            return VerifyOtpResponse.builder()
                    .verified(verified)
                    .message(verified ? "OTP verified successfully (mock)" : "Invalid OTP. Use " + MOCK_OTP + " in mock mode.")
                    .build();
        }

        try {
            TwoFactorResponse response = twoFactorClient.verifyOTP(sessionId, otp);
            // 2Factor returns 200 for both success and failure; check Status and Details
            boolean verified = SUCCESS_STATUS.equalsIgnoreCase(response.getStatus())
                    && response.getDetails() != null
                    && response.getDetails().toLowerCase().contains("matched");
            String message = response.getDetails() != null ? response.getDetails() : "Verification failed";
            return VerifyOtpResponse.builder()
                    .verified(verified)
                    .message(verified ? "OTP verified successfully" : message)
                    .build();
        } catch (Exception e) {
            log.error("Failed to verify OTP: {}", e.getMessage());
            return VerifyOtpResponse.builder()
                    .verified(false)
                    .message("Verification failed: " + e.getMessage())
                    .build();
        }
    }
}
