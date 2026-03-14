package com.indra.todone.controller;

import com.indra.todone.dto.request.InitiateLoginRequest;
import com.indra.todone.dto.request.VerifyLoginRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.dto.response.InitiateLoginResponse;
import com.indra.todone.model.User;
import com.indra.todone.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Login / signup via OTP")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login/initiate")
    @Operation(summary = "Initiate login", description = "Sends OTP to the given phone number. Returns sessionId to use in verify step.")
    public ResponseEntity<ApiResponse<InitiateLoginResponse>> initiateLogin(@RequestBody InitiateLoginRequest request) {
        InitiateLoginResponse response = authService.initiateLogin(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("OTP sent", response));
    }

    @PostMapping("/login/verify")
    @Operation(summary = "Verify login OTP", description = "Verifies OTP using sessionId from initiate. Authenticates the user; if user does not exist, a new account is created and returned.")
    public ResponseEntity<ApiResponse<User>> verifyLogin(@RequestBody VerifyLoginRequest request) {
        User user = authService.verifyLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
    }
}
