package com.indra.todone.controller;

import com.indra.todone.dto.request.SendOtpRequest;
import com.indra.todone.dto.request.VerifyOtpRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.dto.response.SendOtpResponse;
import com.indra.todone.dto.response.VerifyOtpResponse;
import com.indra.todone.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sms")
@Tag(name = "SMS", description = "SMS OTP send and verify APIs")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP", description = "Sends an OTP to the given phone number. Returns a session ID to use for verification. In mock mode (OTP_MOCK=true), no SMS is sent and 0000 is accepted for verify.")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@RequestBody SendOtpRequest request) {
        SendOtpResponse response = smsService.sendOtp(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("OTP sent", response));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP", description = "Verifies the OTP using the session ID from send OTP. In mock mode, use 0000 as the OTP.")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse response = smsService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
