package com.indra.todone.service;

import com.indra.todone.dto.request.CreateUserRequest;
import com.indra.todone.dto.request.UpdateUserRequest;
import com.indra.todone.dto.response.ProfileResponse;
import com.indra.todone.exception.DuplicatePhoneNumberException;
import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import com.indra.todone.model.User;
import com.indra.todone.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskService taskService;

    public UserService(UserRepository userRepository, TaskService taskService) {
        this.userRepository = userRepository;
        this.taskService = taskService;
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
}
