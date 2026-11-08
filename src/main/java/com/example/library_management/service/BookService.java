package com.example.library_management.service;

import com.example.library_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookService {
    List<Book> findAll();
    Optional<Book> findById(Long id);
    Book save(Book book);
    void deleteById(Long id);
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthorId(Long authorId);
    List<Book> findAvailableBooks();
    Optional<Book> findByIsbn(String isbn);
    List<Book> findOverdueBooks();
}