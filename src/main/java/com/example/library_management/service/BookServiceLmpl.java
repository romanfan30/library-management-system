package com.example.library_management.service;

import com.example.library_management.dto.CreateBookDTO;
import com.example.library_management.entity.Author;
import com.example.library_management.entity.Book;
import com.example.library_management.entity.Category;
import com.example.library_management.repository.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookServiceLmpl implements BookService {

    @Autowired
    private BookRepository bookRepository;

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    @Override
    public List<Book> findByAuthorId(Long authorId) {
        return bookRepository.findByAuthorId(authorId);
    }


    @Override
    public List<Book> findAvailableBooks() {
        return bookRepository.findByAvailableCopiesGreaterThan(0);
    }
    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return bookRepository.findByIsbn(isbn);
    }
    @Override
    public List<Book> findOverdueBooks() {
        return bookRepository.findOverdueBooks();
    }
    // Метод для создания книги с автором и категорией
    public Book createBookWithAuthorAndCategory(CreateBookDTO dto, Author author, Category
            category) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setAvailableCopies(dto.getAvailableCopies());
        book.setAuthor(author);
        book.setCategory(category);
        return save(book);
    }
}
