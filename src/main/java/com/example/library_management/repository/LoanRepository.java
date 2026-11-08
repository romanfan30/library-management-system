package com.example.library_management.repository;
import com.example.library_management.entity.Loan;
import com.example.library_management.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Поиск займов пользователя
    List<Loan> findByUserId(Long userId);

    // Поиск активных займов пользователя
    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    // Поиск займов книги
    List<Loan> findByBookId(Long bookId);

    // Поиск просроченных займов
    @Query("SELECT l FROM Loan l WHERE l.dueDate < :currentDate AND l.status = 'ACTIVE'")
    List<Loan> findOverdueLoans(@Param("currentDate") LocalDateTime currentDate);

    // Поиск активных займов
    List<Loan> findByStatus(LoanStatus status);

    // Проверка активного займа книги пользователем
    @Query("SELECT l FROM Loan l WHERE l.book.id = :bookId AND l.user.id = :userId AND l.status = 'ACTIVE'")
    Optional<Loan> findActiveLoanByBookAndUser(@Param("bookId") Long bookId, @Param("userId")
    Long userId);
}