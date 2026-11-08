package com.example.library_management.service;

import com.example.library_management.entity.Author;
import com.example.library_management.entity.Book;
import com.example.library_management.entity.Category;
import com.example.library_management.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit тесты для BookService (практика 5, Шаг 2).
 * Репозиторий замокан — тестируется только логика сервиса, БД не нужна.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceLmpl bookService;

    private Book testBook;
    private Author testAuthor;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testAuthor = new Author("Лев", "Толстой");
        testAuthor.setId(1L);

        testCategory = new Category("Художественная литература", "Романы");
        testCategory.setId(1L);

        testBook = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        testBook.setId(1L);
        testBook.setAuthor(testAuthor);
        testBook.setCategory(testCategory);
    }

    @Test
    void findAll_ShouldReturnAllBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // When
        List<Book> actualBooks = bookService.findAll();

        // Then
        assertEquals(1, actualBooks.size());
        assertEquals("Война и мир", actualBooks.get(0).getTitle());
        verify(bookRepository).findAll();
    }

    @Test
    void findById_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.findById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Война и мир", result.get().getTitle());
        verify(bookRepository).findById(1L);
    }

    @Test
    void findById_WhenBookNotExists_ShouldReturnEmpty() {
        // Given
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Book> result = bookService.findById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(bookRepository).findById(999L);
    }

    @Test
    void save_ShouldReturnSavedBook() {
        // Given
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        Book result = bookService.save(testBook);

        // Then
        assertEquals("Война и мир", result.getTitle());
        verify(bookRepository).save(testBook);
    }

    @Test
    void deleteById_ShouldCallRepository() {
        // Given
        doNothing().when(bookRepository).deleteById(1L);

        // When
        bookService.deleteById(1L);

        // Then
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void findByTitleContaining_ShouldReturnMatchingBooks() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByTitleContainingIgnoreCase("война")).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.findByTitleContaining("война");

        // Then
        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
        verify(bookRepository).findByTitleContainingIgnoreCase("война");
    }

    @Test
    void findByAuthorId_ShouldReturnBooksByAuthor() {
        // Given
        List<Book> expectedBooks = Arrays.asList(testBook);
        when(bookRepository.findByAuthorId(1L)).thenReturn(expectedBooks);

        // When
        List<Book> result = bookService.findByAuthorId(1L);

        // Then
        assertEquals(1, result.size());
        assertEquals("Война и мир", result.get(0).getTitle());
        verify(bookRepository).findByAuthorId(1L);
    }

    // ---- Дополнительно: методы, которые есть в BookService ----

    @Test
    void findAvailableBooks_ShouldReturnOnlyAvailable() {
        // Given
        when(bookRepository.findByAvailableCopiesGreaterThan(0))
                .thenReturn(Arrays.asList(testBook));

        // When
        List<Book> result = bookService.findAvailableBooks();

        // Then
        assertEquals(1, result.size());
        verify(bookRepository).findByAvailableCopiesGreaterThan(0);
    }

    @Test
    void findByIsbn_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findByIsbn("978-5-17-123456-7")).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.findByIsbn("978-5-17-123456-7");

        // Then
        assertTrue(result.isPresent());
        assertEquals("978-5-17-123456-7", result.get().getIsbn());
        verify(bookRepository).findByIsbn("978-5-17-123456-7");
    }

    @Test
    void findOverdueBooks_ShouldDelegateToRepository() {
        // Given
        when(bookRepository.findOverdueBooks()).thenReturn(Arrays.asList(testBook));

        // When
        List<Book> result = bookService.findOverdueBooks();

        // Then
        assertEquals(1, result.size());
        verify(bookRepository).findOverdueBooks();
    }
}
