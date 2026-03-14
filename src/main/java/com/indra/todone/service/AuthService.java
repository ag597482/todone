package com.indra.todone.service;

import com.indra.todone.dto.request.CreateUserRequest;
import com.indra.todone.dto.request.InitiateLoginRequest;
import com.indra.todone.dto.request.SendOtpRequest;
import com.indra.todone.dto.request.VerifyLoginRequest;
import com.indra.todone.dto.request.VerifyOtpRequest;
import com.indra.todone.dto.response.InitiateLoginResponse;
import com.indra.todone.dto.response.SendOtpResponse;
import com.indra.todone.dto.response.VerifyOtpResponse;
import com.indra.todone.exception.DuplicatePhoneNumberException;
import com.indra.todone.exception.InvalidOtpException;
import com.indra.todone.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final SmsService smsService;
    private final UserService userService;

    /** Session ID -> phone number. Cleared after successful verify. */
    private final Map<String, String> loginSessionStore = new ConcurrentHashMap<>();

    public InitiateLoginResponse initiateLogin(InitiateLoginRequest request) {
        String phoneNumber = request.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber is required");
        }

        SendOtpResponse otpResponse = smsService.sendOtp(
                SendOtpRequest.builder()
                        .phoneNumber(phoneNumber)
                        .build());

        String sessionId = otpResponse.getSessionId();
        loginSessionStore.put(sessionId, phoneNumber);

        return InitiateLoginResponse.builder()
                .sessionId(sessionId)
                .message(otpResponse.getMessage())
                .build();
    }

    public User verifyLogin(VerifyLoginRequest request) {
        String sessionId = request.getSessionId();
        String otp = request.getOtp();
        if (sessionId == null || sessionId.isBlank() || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("sessionId and otp are required");
        }

        VerifyOtpResponse verifyResponse = smsService.verifyOtp(
                VerifyOtpRequest.builder()
                        .sessionId(sessionId)
                        .otp(otp)
                        .build());

        if (!verifyResponse.isVerified()) {
            throw new InvalidOtpException(verifyResponse.getMessage());
        }

        String phoneNumber = loginSessionStore.remove(sessionId);
        if (phoneNumber == null) {
            log.warn("Verify succeeded but no session found for sessionId: {}", sessionId);
            throw new IllegalArgumentException("Invalid or expired login session. Please initiate login again.");
        }

        return userService.getByPhoneNumber(phoneNumber)
                .orElseGet(() -> {
                    try {
                        log.info("Creating new user for phone: {}", phoneNumber);
                        return userService.createUser(CreateUserRequest.builder()
                                .phoneNumber(phoneNumber)
                                .name("")
                                .metadata(Map.of())
                                .build());
                    } catch (DuplicatePhoneNumberException e) {
                        return userService.getByPhoneNumber(phoneNumber)
                                .orElseThrow(() -> new IllegalStateException("User creation failed", e));
                    }
                });
    }
}
