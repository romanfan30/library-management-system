package com.example.library_management.dto;

import com.example.library_management.entity.Author;
import com.example.library_management.entity.Book;

import java.time.LocalDate;
import java.util.Locale;

public class BookDTO {

    private Long id;
    private String title;
    private String isbn;
    private LocalDate publicationDate;
    private Integer availableCopies;
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;

    public BookDTO() {}

    public BookDTO(Long id, String title,
                   String isbn, LocalDate publicationDate,
                   Integer availableCopies, Long authorId,
                   String authorName, Long categoryId, String categoryName) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.publicationDate = publicationDate;
        this.availableCopies = availableCopies;
        this.authorId = authorId;
        this.authorName = authorName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public LocalDate getPublicationDate() { return publicationDate; }
    public void setPublicationDate(LocalDate publicationDate) { this.publicationDate =
            publicationDate; }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies =
            availableCopies; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public String getAuthorName() { return authorName; }

    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }


}
