package com.example.library_management;

import com.example.library_management.entity.*;
import com.example.library_management.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Заполняет базу тестовыми данными при старте приложения.
 *
 * ВАЖНО: сид-раннер должен быть идемпотентным (повторный запуск не должен падать),
 * иначе при уже существующих в БД записях приложение не стартует
 * (duplicate key value violates unique constraint).
 *
 * В тестах бин НЕ создаётся (@Profile("!test")). @SpringBootTest поднимает весь
 * контекст приложения, поэтому без этого сид выполнялся бы перед каждым тестовым
 * классом и занимал уникальные email/названия категорий в H2. Тесты в @BeforeEach
 * вставляют те же значения, и Hibernate внутри одной транзакции выполняет INSERT
 * раньше DELETE (порядок ActionQueue), из-за чего падало на duplicate key (23505).
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public DataLoader(AuthorRepository authorRepository,
                      CategoryRepository categoryRepository,
                      BookRepository bookRepository,
                      UserRepository userRepository) {
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {

        // ---------- Категории ----------
        Category fiction = categoryRepository.findByName("Художественная литература")
                .orElseGet(() -> categoryRepository.save(
                        new Category("Художественная литература", "Романы, повести, рассказы")));

        Category science = categoryRepository.findByName("Научная литература")
                .orElseGet(() -> categoryRepository.save(
                        new Category("Научная литература", "Учебники, научные труды")));

        Category history = categoryRepository.findByName("История")
                .orElseGet(() -> categoryRepository.save(
                        new Category("История", "Исторические книги")));

        // ---------- Авторы ----------
        Author tolstoy = findOrCreateAuthor("Лев", "Толстой",
                LocalDate.of(1828, 9, 9), "Русский писатель и мыслитель");

        Author pushkin = findOrCreateAuthor("Александр", "Пушкин",
                LocalDate.of(1799, 6, 6), "Русский поэт, драматург и прозаик");

        // ---------- Книги ----------
        createBookIfAbsent("Война и мир", "978-5-17-123456-7",
                LocalDate.of(1869, 1, 1), 5, tolstoy, fiction);

        createBookIfAbsent("Евгений Онегин", "978-5-17-123457-4",
                LocalDate.of(1833, 1, 1), 3, pushkin, fiction);

        // ---------- Пользователи ----------
        createUserIfAbsent("admin@library.com", "admin123", "Администратор", "Системы");
        createUserIfAbsent("librarian@library.com", "librarian123", "Библиотекарь", "Главный");

        System.out.println("Тестовые данные загружены успешно!");
    }

    private Author findOrCreateAuthor(String firstName, String lastName,
                                      LocalDate birthDate, String biography) {
        return authorRepository.findAll().stream()
                .filter(a -> firstName.equalsIgnoreCase(a.getFirstName())
                        && lastName.equalsIgnoreCase(a.getLastName()))
                .findFirst()
                .map(existing -> {
                    // дозаполняем поля, если их не было
                    if (existing.getBirthDate() == null) existing.setBirthDate(birthDate);
                    if (existing.getBiography() == null) existing.setBiography(biography);
                    return authorRepository.save(existing);
                })
                .orElseGet(() -> {
                    Author author = new Author(firstName, lastName);
                    author.setBirthDate(birthDate);
                    author.setBiography(biography);
                    return authorRepository.save(author);
                });
    }

    private void createBookIfAbsent(String title, String isbn, LocalDate publicationDate,
                                    Integer availableCopies, Author author, Category category) {
        if (bookRepository.findByIsbn(isbn).isPresent()) {
            return;
        }
        Book book = new Book(title, isbn, publicationDate, availableCopies);
        book.setAuthor(author);
        book.setCategory(category);
        bookRepository.save(book);
    }

    private void createUserIfAbsent(String email, String password,
                                    String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            return;
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
