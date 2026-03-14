package com.indra.todone.controller;

import com.indra.todone.dto.request.CreateUserRequest;
import com.indra.todone.dto.request.UpdateUserRequest;
import com.indra.todone.dto.response.ApiResponse;
import com.indra.todone.model.User;
import com.indra.todone.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User management APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user with name, phone number and optional metadata")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User created successfully", user));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Returns a list of all users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Returns a single user with the given user_id. Returns 404 if not found.")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String userId) {
        return userService.getByUserId(userId)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("User not found")));
    }

    @GetMapping("/by-phone/{phoneNumber}")
    @Operation(summary = "Get user by phone number", description = "Returns a single user matching the given phone number")
    public ResponseEntity<ApiResponse<User>> getUserByPhoneNumber(@PathVariable String phoneNumber) {
        return userService.getByPhoneNumber(phoneNumber)
                .map(user -> ResponseEntity.ok(ApiResponse.success(user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("User not found")));
    }

    @PutMapping("/by-phone/{phoneNumber}")
    @Operation(summary = "Update user by phone number", description = "Updates name and/or metadata for the user with the given phone number. Only provided fields are updated.")
    public ResponseEntity<ApiResponse<User>> updateUserByPhoneNumber(
            @PathVariable String phoneNumber,
            @RequestBody UpdateUserRequest request) {
        return userService.updateByPhoneNumber(phoneNumber, request)
                .map(user -> ResponseEntity.ok(ApiResponse.success("User updated successfully", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("User not found")));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user by ID", description = "Deletes the user with the given user_id. Returns 204 on success, 404 if user not found.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String userId) {
        if (userService.deleteByUserId(userId)) {
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("User not found"));
    }
}
