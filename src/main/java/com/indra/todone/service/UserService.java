package com.indra.todone.service;

import com.indra.todone.dto.request.CreateUserRequest;
import com.indra.todone.dto.request.UpdateUserRequest;
import com.indra.todone.exception.DuplicatePhoneNumberException;
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

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
