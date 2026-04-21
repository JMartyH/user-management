package com.example.demo.service.impl;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserRequest userRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userRequest = new UserRequest("testuser", "test@example.com", "password123", "Test User");
        
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .status(User.UserStatus.ACTIVE)
                .build();

        userResponse = new UserResponse(
                userId,
                "testuser",
                "test@example.com",
                User.UserStatus.ACTIVE,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("createUser Tests")
    class CreateUserTests {
        @Test
        @DisplayName("Should successfully create a user")
        void createUser_Success() {
            when(userRepository.existsByUsername(userRequest.username())).thenReturn(false);
            when(userRepository.existsByEmail(userRequest.email())).thenReturn(false);
            when(passwordEncoder.encode(userRequest.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.createUser(userRequest);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(userRequest.username());
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode(userRequest.password());
        }

        @Test
        @DisplayName("Should throw exception when username exists")
        void createUser_UsernameExists_ThrowsException() {
            when(userRepository.existsByUsername(userRequest.username())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(userRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Username already exists");
            
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when email exists")
        void createUser_EmailExists_ThrowsException() {
            when(userRepository.existsByUsername(userRequest.username())).thenReturn(false);
            when(userRepository.existsByEmail(userRequest.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(userRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already exists");
        }
    }

    @Nested
    @DisplayName("getUserById Tests")
    class GetUserByIdTests {
        @Test
        void getUserById_Found() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserById(userId);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(userId);
        }

        @Test
        void getUserById_NotFound() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id");
        }
    }

    @Nested
    @DisplayName("getUserByUsername Tests")
    class GetByUsernameTests {
        @Test
        void getUserByUsername_Found() {
            String username = "testuser";
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserByUsername(username);

            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo(username);
        }

        @Test
        void getUserByUsername_NotFound() {
            String username = "nonexistent";
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByUsername(username))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with username");
        }
    }

    @Nested
    @DisplayName("getAllUsers Tests")
    class GetAllUsersTests {
        @Test
        void getAllUsers_Success() {
            List<User> users = List.of(user);
            List<UserResponse> responses = List.of(userResponse);
            when(userRepository.findAll()).thenReturn(users);
            when(userMapper.toResponseList(users)).thenReturn(responses);

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).username()).isEqualTo(user.getUsername());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {
        @Test
        void updateUser_Success() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateUser(userId, userRequest);

            assertThat(result).isNotNull();
            verify(userRepository).save(user);
        }

        @Test
        void updateUser_NotFound_ThrowsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(userId, userRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {
        @Test
        void deleteUser_Success() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            userService.deleteUser(userId);

            verify(userRepository).delete(user);
        }

        @Test
        void deleteUser_NotFound_ThrowsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteUser(userId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("updateStatus Tests")
    class UpdateStatusTests {
        @Test
        void updateStatus_Success() {
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateStatus(userId, User.UserStatus.INACTIVE);

            assertThat(result).isNotNull();
            assertThat(user.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
            verify(userRepository).save(user);
        }

        @Test
        void updateStatus_NotFound_ThrowsException() {
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateStatus(userId, User.UserStatus.INACTIVE))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found");
        }
    }

    @Nested
    @DisplayName("login Tests")
    class LoginTests {
        @Test
        void login_Success() {
            String username = "testuser";
            String password = "password123";
            String token = "jwt.token.here";

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
            when(jwtUtils.generateToken(username)).thenReturn(token);

            String result = userService.login(username, password);

            assertThat(result).isEqualTo(token);
        }

        @Test
        void login_InvalidPassword_ThrowsException() {
            String username = "testuser";
            String password = "wrongpassword";

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> userService.login(username, password))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");
        }

        @Test
        void login_UserNotFound_ThrowsException() {
            String username = "nonexistent";
            String password = "password123";

            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(username, password))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("Invalid username or password");
        }
    }
}
