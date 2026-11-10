# Library Management

Система управления библиотекой на Spring Boot: книги, авторы, категории, пользователи, выдача книг и простая аутентификация.

Проект собран по практикам 1–5:

| Практика | Тема | Что добавлено |
|---|---|---|
| 1 | Создание проекта и базовых классов | сущности, сервисы, `application.properties` |
| 2 | Spring Data JPA и репозитории | 5 репозиториев, `DataLoader` с тестовыми данными |
| 3 | REST API | DTO, `BookController`, `UserController` |
| 4 | Аутентификация и авторизация | Spring Security, `/api/auth/login`, `CustomUserDetailsService` |
| 5 | Тестирование | 50 тестов: unit, интеграционные, репозиториев |

## Стек

- Java 17, Spring Boot 4.1.1
- Spring Web MVC, Spring Data JPA (Hibernate), Spring Security
- PostgreSQL (рабочая БД), H2 (тесты)
- Bean Validation, Maven

## Что реализовано

### Сущности

`Book`, `Author`, `Category`, `User`, `Loan` (+ `LoanStatus`: `ACTIVE`, `RETURNED`, `OVERDUE`) — со связями `@ManyToOne` / `@OneToMany` и ограничениями (`unique` на `users.email` и `categories.name`).

### Репозитории

`BookRepository`, `AuthorRepository`, `CategoryRepository`, `UserRepository`, `LoanRepository` — наследуют `JpaRepository` и содержат методы поиска: по названию, ISBN, автору, категории, доступным копиям, email, активным займам, плюс кастомные `@Query`.

### Сервисы

`BookService` / `BookServiceLmpl`, `UserService` / `UserServiceLmpl` — CRUD и поиск. `UserServiceLmpl.save()` кодирует пароль через `PasswordEncoder` (bcrypt), поэтому в БД не попадают пароли открытым текстом.

`CustomUserDetailsService` — загружает пользователя по email для Spring Security.

### REST API

**Книги** — `/api/books`

| Метод | URL | Действие |
|---|---|---|
| GET | `/api/books` | все книги |
| GET | `/api/books/{id}` | книга по ID (404, если нет) |
| POST | `/api/books` | создать книгу (201) |
| PUT | `/api/books/{id}` | обновить книгу |
| DELETE | `/api/books/{id}` | удалить книгу (204) |
| GET | `/api/books/search?title=война` | поиск по названию |
| GET | `/api/books/available` | только доступные книги |

**Пользователи** — `/api/users`

| Метод | URL | Действие |
|---|---|---|
| GET | `/api/users` | все пользователи |
| GET | `/api/users/{id}` | пользователь по ID |
| GET | `/api/users/email/{email}` | пользователь по email |

В ответах используется `UserDTO` — пароль никогда не отдаётся клиенту.

**Аутентификация** — `/api/auth`

| Метод | URL | Действие |
|---|---|---|
| POST | `/api/auth/login` | вход по `email` + `password` |
| POST | `/api/auth/logout` | выход |

Пример входа:

```json
{
  "email": "admin@library.com",
  "password": "password123"
}
```

Ответ `200`:

```json
{
  "message": "Успешный вход в систему",
  "userId": 1,
  "email": "admin@library.com",
  "role": "USER"
}
```

### Безопасность

- `SecurityConfig` — `@EnableWebSecurity`, `@EnableMethodSecurity`, `BCryptPasswordEncoder`
- `/api/auth/**` открыт, `/api/**` требует аутентификации, остальное — тоже
- без аутентификации `GET /api/books` возвращает **401** (не редирект на форму логина)
- сессии — `IF_REQUIRED`

### Тестовые данные

`DataLoader` (`CommandLineRunner`) при старте создаёт категории, авторов, книги и пользователей, если их ещё нет в БД. В тестах бин отключён (`@Profile("!test")`), поэтому тесты работают на чистой H2.

### Тесты

50 тестов, все проходят без запущенного PostgreSQL:

| Файл | Тестов | Тип |
|---|---|---|
| `BookServiceTest` | 10 | unit, Mockito |
| `UserServiceTest` | 7 | unit, Mockito + `PasswordEncoder` |
| `BookControllerIntegrationTest` | 9 | `@SpringBootTest` + MockMvc |
| `UserControllerIntegrationTest` | 5 | `@SpringBootTest` + MockMvc |
| `AuthControllerIntegrationTest` | 5 | безопасность: 200 / 400 / 401 |
| `BookRepositoryTest` | 8 | `@DataJpaTest` |
| `UserRepositoryTest` | 5 | `@DataJpaTest` |
| `LibraryManagementApplicationTests` | 1 | `contextLoads` |

## Структура проекта

```
src/main/java/com/example/library_management/
├── LibraryManagementApplication.java   # Главный класс приложения
├── DataLoader.java                     # Тестовые данные при старте
├── config/                             # Конфигурация (настройки приложения)
│   └── SecurityConfig.java             # Spring Security
├── controller/                         # REST контроллеры (обработка HTTP запросов)
│   ├── AuthController.java
│   ├── BookController.java
│   └── UserController.java
├── service/                            # Бизнес-логика (основная логика приложения)
│   ├── BookService.java
│   ├── BookServiceLmpl.java
│   ├── UserService.java
│   ├── UserServiceLmpl.java
│   └── CustomUserDetailsService.java
├── repository/                         # Репозитории для работы с БД (доступ к данным)
│   ├── BookRepository.java
│   ├── AuthorRepository.java
│   ├── CategoryRepository.java
│   ├── UserRepository.java
│   └── LoanRepository.java
├── entity/                             # Сущности (модели данных, таблицы БД)
│   ├── Book.java
│   ├── Author.java
│   ├── Category.java
│   ├── User.java
│   ├── Loan.java
│   └── LoanStatus.java
└── dto/                                # Data Transfer Objects (объекты для передачи данных)
    ├── BookDTO.java
    ├── CreateBookDTO.java
    ├── UserDTO.java
    └── LoginRequest.java

src/main/resources/
├── application.properties              # локальная конфигурация (в .gitignore)
└── application.properties.example      # шаблон, копируется вручную

src/test/java/com/example/library_management/
├── LibraryManagementApplicationTests.java
├── controller/                         # интеграционные тесты контроллеров
├── service/                            # unit тесты сервисов
└── repository/                         # тесты репозиториев

src/test/resources/
└── application-test.properties         # H2 в памяти для тестов
```

## Запуск

### 1. База данных

```sql
CREATE DATABASE library_db;
```

Схему таблиц Hibernate создаёт сам (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Конфигурация

`application.properties` **не хранится в репозитории** (в нём логин и пароль от БД). Скопируйте шаблон и укажите свой пароль:

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

```properties
spring.datasource.username=postgres
spring.datasource.password=ваш_пароль
```

### 3. Запуск

```bash
./mvnw spring-boot:run
```

Или запустить `LibraryManagementApplication` из IntelliJ IDEA. Приложение поднимется на `http://localhost:8080`.

### 4. Тесты

```bash
./mvnw test
```

Ожидаемо: `Tests run: 50, Failures: 0, Errors: 0, Skipped: 0`.

### Пользователи для проверки

| Email | Пароль |
|---|---|
| `admin@library.com` | `password123` |
| `user@library.com` | `password123` |

Если пользователи созданы `DataLoader` (пароль `admin123` открытым текстом), для входа нужны записи с bcrypt-хешем — см. практику 4, Шаг 6:

```sql
UPDATE users
SET password = '$2a$10$YwzpckiB/VhXrQO57wHJMu8iPS3fjeFWpPD06KDC/xWhKAKrFr782';
```

Этот хеш соответствует паролю `password123`.

