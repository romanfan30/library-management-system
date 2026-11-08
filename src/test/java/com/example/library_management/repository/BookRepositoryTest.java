package com.example.library_management.repository;

import com.example.library_management.entity.Author;
import com.example.library_management.entity.Book;
import com.example.library_management.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты репозитория книг (практика 5, задание: тесты для репозиториев).
 * @DataJpaTest поднимает только JPA-слой на встроенной H2 и откатывает транзакцию.
 */
@DataJpaTest
@ActiveProfiles("test")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        author = authorRepository.save(new Author("Лев", "Толстой"));
        category = categoryRepository.save(new Category("Художественная литература", "Романы"));
    }

    private Book persistBook(String title, String isbn, int copies) {
        Book book = new Book(title, isbn, LocalDate.of(1869, 1, 1), copies);
        book.setAuthor(author);
        book.setCategory(category);
        return bookRepository.save(book);
    }

    @Test
    void findByIsbn_WhenExists_ShouldReturnBook() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);
        entityManager.flush();

        Optional<Book> result = bookRepository.findByIsbn("978-5-17-123456-7");

        assertTrue(result.isPresent());
        assertEquals("Война и мир", result.get().getTitle());
    }

    @Test
    void findByIsbn_WhenNotExists_ShouldReturnEmpty() {
        Optional<Book> result = bookRepository.findByIsbn("000-0-00-000000-0");

        assertFalse(result.isPresent());
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldBeCaseInsensitive() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);
        persistBook("Евгений Онегин", "978-5-17-123457-4", 3);

        List<Book> result = bookRepository.findByTitleContainingIgnoreCase("ВОЙНА");

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
    }

    @Test
    void findByAuthorId_ShouldReturnBooksOfAuthor() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);
        persistBook("Анна Каренина", "978-5-17-123459-8", 2);

        List<Book> result = bookRepository.findByAuthorId(author.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findByAvailableCopiesGreaterThan_ShouldFilterOutZero() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);
        persistBook("Евгений Онегин", "978-5-17-123457-4", 0);

        List<Book> result = bookRepository.findByAvailableCopiesGreaterThan(0);

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
    }

    @Test
    void findByTitleAndAuthor_ShouldUseCustomQuery() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);
        persistBook("Евгений Онегин", "978-5-17-123457-4", 3);

        // В BookRepository запрос написан как LIKE :title (без %), поэтому
        // подставляем маску сами — иначе ищется только точное совпадение.
        List<Book> result = bookRepository.findByTitleAndAuthor("%Война%", "Толстой");

        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
    }

    @Test
    void findByTitleAndAuthor_WhenAuthorDoesNotMatch_ShouldReturnEmpty() {
        persistBook("Война и мир", "978-5-17-123456-7", 5);

        List<Book> result = bookRepository.findByTitleAndAuthor("%Война%", "Пушкин");

        assertTrue(result.isEmpty());
    }

    @Test
    void save_ShouldPersistBookWithRelations() {
        Book saved = persistBook("Война и мир", "978-5-17-123456-7", 5);
        entityManager.flush();
        entityManager.clear();

        Book found = bookRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Лев", found.getAuthor().getFirstName());
        assertEquals("Художественная литература", found.getCategory().getName());
    }
}
