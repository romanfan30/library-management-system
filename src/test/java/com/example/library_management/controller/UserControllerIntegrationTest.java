package com.example.library_management.controller;

import com.example.library_management.entity.User;
import com.example.library_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration тесты для UserController (практика 5, задание: тесты для всех контроллеров).
 * Пароль в ответе не должен появляться — это проверяет UserDTO.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Hibernate в одной транзакции выполняет INSERT раньше DELETE: без flush
        // удаление не доходит до БД и вставка test@library.com может упасть на
        // duplicate key (users.email unique), если строка осталась от DataLoader
        // или от другого контекста.
        entityManager.flush();

        user = new User();
        user.setEmail("test@library.com");
        user.setPassword("$2a$10$VYCbwYzZQ8.kF1dnwKbtiOjwn8KzGvan7TWTRs6HVI8CZgin3OSqS");
        user.setFirstName("Тест");
        user.setLastName("Пользователь");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("test@library.com"))
                // пароль наружу не отдаём
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.firstName").value("Тест"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByEmail_WhenUserExists_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "test@library.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@library.com"));
    }

    @Test
    void getUserByEmail_WhenUserNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "nonexistent@library.com"))
                .andExpect(status().isNotFound());
    }
}
