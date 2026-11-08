package com.example.library_management.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.example.library_management.repository.UserRepository;

import jakarta.persistence.EntityManager;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тесты безопасности на уровне HTTP (практика 4 + 5).
 * Здесь security-фильтры ВКЛЮЧЕНЫ — проверяем именно то, что требует практика 4:
 * - /api/books без аутентификации -> 401;
 * - /api/auth/login с верными данными -> 200 и JSON;
 * - /api/auth/login с неверным паролем -> 400 "Неверный email или пароль"
 *   (именно 400, как возвращает AuthController из методички).
 *
 * Пользователи и пароль создаются в @BeforeEach заново, поэтому тест не зависит
 * от того, что лежит в рабочей БД.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    private static final String PASSWORD = "password123";

    // bcrypt-хеш пароля password123
    private static final String BCRYPT_PASSWORD123 =
            "$2a$10$YwzpckiB/VhXrQO57wHJMu8iPS3fjeFWpPD06KDC/xWhKAKrFr782";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Hibernate в одной транзакции выполняет INSERT раньше DELETE: без flush
        // удаление не доходит до БД, и вставка admin@library.com падает на
        // duplicate key (users.email unique).
        entityManager.flush();

        com.example.library_management.entity.User user =
                new com.example.library_management.entity.User();
        user.setEmail("admin@library.com");
        user.setPassword(BCRYPT_PASSWORD123);
        user.setFirstName("Администратор");
        user.setLastName("Системы");
        user.setCreatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
    }

    @Test
    void getBooks_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@library.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Успешный вход в систему"))
                .andExpect(jsonPath("$.email").value("admin@library.com"));
    }

    @Test
    void login_WithWrongPassword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@library.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Неверный email или пароль"));
    }

    @Test
    void login_WithUnknownEmail_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@library.com\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithoutPassword_ShouldReturnBadRequestByValidation() throws Exception {
        // @NotBlank на LoginRequest.password
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@library.com\"}"))
                .andExpect(status().isBadRequest());
    }
}
