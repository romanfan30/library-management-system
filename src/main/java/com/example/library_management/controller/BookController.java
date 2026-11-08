package com.example.library_management.controller;

import com.example.library_management.dto.BookDTO;
import com.example.library_management.dto.CreateBookDTO;
import com.example.library_management.entity.Book;
import com.example.library_management.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/books")
public class BookController {
    @Autowired
    private BookService bookService;

    // Получить все книги
    @GetMapping
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        List<Book> books = bookService.findAll();
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }
    // Получить книгу по ID
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);
        if (book.isPresent()) {
            return ResponseEntity.ok(convertToDTO(book.get()));
        } else {

            return ResponseEntity.notFound().build();
        }
    }
    // Создать новую книгу
    @PostMapping
    public ResponseEntity<BookDTO> createBook(@Valid @RequestBody CreateBookDTO createBookDTO) {
        try {
            Book book = convertToEntity(createBookDTO);
            Book savedBook = bookService.save(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    // Обновить книгу
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable Long id, @Valid @RequestBody CreateBookDTO createBookDTO) {
        Optional<Book> existingBook = bookService.findById(id);
        if (existingBook.isPresent()) {
            try {
                Book book = convertToEntity(createBookDTO);
                book.setId(id);
                Book updatedBook = bookService.save(book);
                return ResponseEntity.ok(convertToDTO(updatedBook));
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // Удалить книгу
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        Optional<Book> book = bookService.findById(id);
        if (book.isPresent()) {
            bookService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    // Поиск книг по названию
    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(@RequestParam String title) {
        List<Book> books = bookService.findByTitleContaining(title);
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)

                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }
    // Получить доступные книги
    @GetMapping("/available")
    public ResponseEntity<List<BookDTO>> getAvailableBooks() {
        List<Book> books = bookService.findAvailableBooks();
        List<BookDTO> bookDTOs = books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookDTOs);
    }
    // Конвертация Entity в DTO
    private BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setIsbn(book.getIsbn());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setAvailableCopies(book.getAvailableCopies());
        if (book.getAuthor() != null) {
            dto.setAuthorId(book.getAuthor().getId());
            dto.setAuthorName(book.getAuthor().getFirstName() + " " +
                    book.getAuthor().getLastName());
        }
        if (book.getCategory() != null) {
            dto.setCategoryId(book.getCategory().getId());
            dto.setCategoryName(book.getCategory().getName());
        }
        return dto;
    }
    // Конвертация DTO в Entity
    private Book convertToEntity(CreateBookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setAvailableCopies(dto.getAvailableCopies());
// Здесь нужно будет получить Author и Category по ID
// Это можно сделать через сервисы
        return book;
    }
}