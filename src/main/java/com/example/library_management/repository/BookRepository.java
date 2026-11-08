package com.example.library_management.repository;


import com.example.library_management.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByAuthorId(Long authorId);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAvailableCopiesGreaterThan(Integer copies);



    @Query("SELECT b FROM Book b WHERE b.title LIKE :title AND b.author.lastName = :authorLastName")
    List<Book> findByTitleAndAuthor(@Param("title") String title,
                                    @Param("authorLastName") String authorLastName);

    @Query("SELECT DISTINCT b FROM Book b " +
            "JOIN Loan l ON l.book = b " +
            "WHERE l.dueDate < CURRENT_TIMESTAMP " +
            "AND l.returnDate IS NULL")
    List<Book> findOverdueBooks();
}
