package com.example.demo.service.impl;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        log.info("Creating new user with username: {}", userRequest.username());

        if (userRepository.existsByUsername(userRequest.username())) {
            throw new RuntimeException("Username already exists: " + userRequest.username());
        }
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new RuntimeException("Email already exists: " + userRequest.email());
        }

        User user = User.builder()
                .username(userRequest.username())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password()))
                .status(User.UserStatus.ACTIVE)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userMapper.toResponseList(userRepository.findAll());
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRequest userRequest) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!existingUser.getUsername().equals(userRequest.username()) &&
                userRepository.existsByUsername(userRequest.username())) {
            throw new RuntimeException("Username already exists: " + userRequest.username());
        }

        if (!existingUser.getEmail().equals(userRequest.email()) &&
                userRepository.existsByEmail(userRequest.email())) {
            throw new RuntimeException("Email already exists: " + userRequest.email());
        }

        existingUser.setUsername(userRequest.username());
        existingUser.setEmail(userRequest.email());
        existingUser.setPassword(userRequest.password());

        return userMapper.toResponse(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(UUID id, User.UserStatus status) {
        log.info("Updating status to {} for user with id: {}", status, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus(status);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public String login(String username, String password) {
        log.info("Attempting login for username: {}", username);
        return userRepository.findByUsername(username)
                .filter(user -> {
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) log.warn("Login failed for username: {} - incorrect password", username);
                    return matches;
                })
                .map(user -> {
                    log.info("Login successful for username: {}", username);
                    return jwtUtils.generateToken(user.getUsername());
                })
                .orElseThrow(() -> {
                    log.warn("Login failed for username: {} - user not found or bad password", username);
                    return new BadCredentialsException("Invalid username or password");
                });
    }
}
