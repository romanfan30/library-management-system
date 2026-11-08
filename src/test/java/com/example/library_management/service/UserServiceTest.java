package com.example.library_management.service;

import com.example.library_management.entity.User;
import com.example.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для UserService (практика 5, Шаг 2).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceLmpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@library.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Тест");
        testUser.setLastName("Пользователь");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void save_NewUser_ShouldEncodePassword() {
        // Given
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.save(testUser);

        // Then
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(testUser);
        assertEquals("test@library.com", result.getEmail());
    }

    @Test
    void save_AlreadyHashedPassword_ShouldNotEncodeTwice() {
        // Given: пароль уже в виде bcrypt-хеша
        String bcrypt = "$2a$10$VYCbwYzZQ8.kF1dnwKbtiOjwn8KzGvan7TWTRs6HVI8CZgin3OSqS";
        testUser.setPassword(bcrypt);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        userService.save(testUser);

        // Then
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals(bcrypt, testUser.getPassword());
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("test@library.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@library.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@library.com", result.get().getEmail());
        verify(userRepository).findByEmail("test@library.com");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        // Given
        when(userRepository.findByEmail("nonexistent@library.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail("nonexistent@library.com");

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findByEmail("nonexistent@library.com");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        List<User> expected = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(expected);

        // When
        List<User> result = userService.findAll();

        // Then
        assertEquals(1, result.size());
        assertEquals("test@library.com", result.get(0).getEmail());
        verify(userRepository).findAll();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void deleteById_ShouldCallRepository() {
        // When
        userService.deleteById(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }
}
