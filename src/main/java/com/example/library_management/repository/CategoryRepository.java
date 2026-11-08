package com.example.library_management.repository;

import com.example.library_management.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Поиск по названию
    Optional<Category> findByName(String name);
    // Поиск по названию (частичное совпадение)
    List<Category> findByNameContainingIgnoreCase(String name);
    // Проверка существования категории
    boolean existsByName(String name);
}