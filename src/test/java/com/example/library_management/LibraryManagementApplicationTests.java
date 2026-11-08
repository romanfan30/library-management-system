package com.example.library_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Проверка, что контекст приложения поднимается целиком.
 * Профиль test -> H2 в памяти, рабочая PostgreSQL не затрагивается.
 */
@SpringBootTest
@ActiveProfiles("test")
class LibraryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
