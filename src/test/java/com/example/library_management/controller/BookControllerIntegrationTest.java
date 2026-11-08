package com.example.library_management.controller;

import com.example.library_management.dto.CreateBookDTO;
import com.example.library_management.entity.Author;
import com.example.library_management.entity.Book;
import com.example.library_management.entity.Category;
import com.example.library_management.repository.AuthorRepository;
import com.example.library_management.repository.BookRepository;
import com.example.library_management.repository.CategoryRepository;
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
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration тесты для BookController (практика 5, Шаг 3).
 *
 * Отличия от методички, обязательные для Spring Boot 4 / Security 7:
 * - @AutoConfigureMockMvc переехал в org.springframework.boot.webmvc.test.autoconfigure
 *   и его нужно указывать явно (@SpringBootTest больше не настраивает MockMvc);
 * - addFilters = false отключает security-фильтры: иначе с практики 4
 *   /api/** закрыт и все запросы получали бы 401;
 * - @ActiveProfiles("test") — тесты работают на H2 в памяти, а не на рабочей PostgreSQL;
 * - ObjectMapper из Jackson 3 (tools.jackson.databind), иначе JSON пишется не тем маппером.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Author author;
    private Category category;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        categoryRepository.deleteAll();
        // Hibernate внутри одной транзакции выполняет INSERT раньше DELETE, поэтому
        // без явного flush удаление не доходит до БД, и вставка тех же уникальных
        // значений (categories.name) падает на duplicate key.
        flushDeletes();

        author = new Author("Лев", "Толстой");
        author = authorRepository.save(author);

        category = new Category("Художественная литература", "Романы");
        category = categoryRepository.save(category);
    }

    @Test
    void getAllBooks_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createBook_ShouldReturnCreatedBook() throws Exception {
        CreateBookDTO createBookDTO = new CreateBookDTO();
        createBookDTO.setTitle("Война и мир");
        createBookDTO.setIsbn("978-5-17-123456-7");
        createBookDTO.setPublicationDate(LocalDate.of(1869, 1, 1));
        createBookDTO.setAvailableCopies(5);
        createBookDTO.setAuthorId(author.getId());
        createBookDTO.setCategoryId(category.getId());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBookDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Война и мир"))
                .andExpect(jsonPath("$.isbn").value("978-5-17-123456-7"));
    }

    @Test
    void createBook_WithInvalidBody_ShouldReturnBadRequest() throws Exception {
        // title пустой -> @NotBlank на CreateBookDTO
        CreateBookDTO invalid = new CreateBookDTO();
        invalid.setIsbn("978-5-17-123456-7");
        invalid.setPublicationDate(LocalDate.of(1869, 1, 1));
        invalid.setAvailableCopies(5);
        invalid.setAuthorId(author.getId());
        invalid.setCategoryId(category.getId());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookById_WhenBookExists_ShouldReturnBook() throws Exception {
        Book book = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        book.setAuthor(author);
        book.setCategory(category);
        book = bookRepository.save(book);

        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(book.getId()))
                .andExpect(jsonPath("$.title").value("Война и мир"));
    }

    @Test
    void getBookById_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        Book book = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        book.setAuthor(author);
        book.setCategory(category);
        bookRepository.save(book);

        mockMvc.perform(get("/api/books/search")
                        .param("title", "война"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Война и мир"));
    }

    @Test
    void getAvailableBooks_ShouldReturnOnlyAvailable() throws Exception {
        Book available = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        available.setAuthor(author);
        available.setCategory(category);

        Book unavailable = new Book("Евгений Онегин", "978-5-17-123457-4",
                LocalDate.of(1833, 1, 1), 0);
        unavailable.setAuthor(author);
        unavailable.setCategory(category);

        bookRepository.save(available);
        bookRepository.save(unavailable);

        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Война и мир"));
    }

    @Test
    void deleteBook_WhenBookExists_ShouldReturnNoContent() throws Exception {
        Book book = new Book("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5);
        book.setAuthor(author);
        book.setCategory(category);
        book = bookRepository.save(book);
        Long id = book.getId();

        mockMvc.perform(delete("/api/books/{id}", id))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.findById(id).isPresent());
    }

    @Test
    void deleteBook_WhenBookNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound());
    }

    private void flushDeletes() {
        entityManager.flush();
    }
}
