package com.example.library_management.repository;

import com.example.library_management.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты репозитория пользователей (практика 5, задание: тесты для репозиториев).
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("$2a$10$VYCbwYzZQ8.kF1dnwKbtiOjwn8KzGvan7TWTRs6HVI8CZgin3OSqS");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Test
    void findByEmail_WhenExists_ShouldReturnUser() {
        newUser("test@library.com", "Тест", "Пользователь");

        Optional<User> result = userRepository.findByEmail("test@library.com");

        assertTrue(result.isPresent());
        assertEquals("Тест", result.get().getFirstName());
    }

    @Test
    void findByEmail_WhenNotExists_ShouldReturnEmpty() {
        assertFalse(userRepository.findByEmail("nonexistent@library.com").isPresent());
    }

    @Test
    void existsByEmail_ShouldReflectDatabaseState() {
        newUser("admin@library.com", "Администратор", "Системы");

        assertTrue(userRepository.existsByEmail("admin@library.com"));
        assertFalse(userRepository.existsByEmail("other@library.com"));
    }

    @Test
    void findByFirstNameContainingIgnoreCaseOrLastNameIgnoreCase_ShouldFindByLastName() {
        newUser("tolstoy@library.com", "Лев", "Толстой");
        newUser("pushkin@library.com", "Александр", "Пушкин");

        // метод называется ...OrLastNameIgnoreCase (без Containing), поэтому
        // по фамилии работает точное совпадение
        List<User> result =
                userRepository.findByFirstNameContainingIgnoreCaseOrLastNameIgnoreCase("Лев", "Толстой");

        assertEquals(1, result.size());
        assertEquals("Толстой", result.get(0).getLastName());
    }

    @Test
    void save_ShouldPersistUser() {
        User saved = newUser("new@library.com", "Новый", "Пользователь");

        assertNotNull(saved.getId());
        assertEquals(1, userRepository.findAll().size());
    }
}
