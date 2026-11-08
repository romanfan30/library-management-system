package com.example.library_management.repository;

import com.example.library_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByFirstNameContainingIgnoreCaseOrLastNameIgnoreCase(String firstName, String lastName);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.loans l WHERE l.status = 'ACTIVE'")
    List<User> findusersWhithActiveLoans();
}
