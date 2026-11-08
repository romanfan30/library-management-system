package com.example.library_management.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class CreateBookDTO {
    @NotBlank(message = "Название киги обязательно")
    private String title;

    @NotBlank(message = "ISBN обязателен")
    private String isbn;

    @NotNull(message = "Дата публикации обязательна")
    private LocalDate publicationDate;

    @NotNull(message = "Количество копий обязательно")
    @Positive(message = "Количество копий должно быть положительным")
    private Integer availableCopies;

    @NotNull(message = "ID автора обязателен")
    private Long authorId;

    @NotNull(message = "ID категории обязателен")
    private Long categoryId;

    // Конструкторы, геттеры, сеттеры
    public CreateBookDTO() {}
    // Геттеры и сеттеры
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
