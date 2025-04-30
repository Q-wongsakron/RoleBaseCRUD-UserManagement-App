# User Management System (Spring Boot + MySQL + JWT)

A secure RESTful API for user registration, login, role-based access control, and token management using Spring Boot, MySQL, and JWT.

---

## Features

- User Registration & Login
- JWT Authentication (Access & Refresh Tokens)
- Role-Based Authorization (ADMIN / USER)
- CRUD Application
- Secure Password Encryption (BCrypt)
- Clean MVC Architecture
- CORS Configuration
- API Testing via Postman

---

## Tech Stack

- **Spring Boot**
- **Spring Security**
- **JWT (JSON Web Tokens)**
- **MySQL**
- **JPA / Hibernate**
- **Lombok**
- **Postman** (for testing)

---

## Spring Security + JWT

### JWT Workflow

- Login returns an **Access Token** and **Refresh Token**
- Access token is short-lived (e.g., 24 hours)
- Refresh token can be used to generate a new access token

### Main Components:

- `SecurityFilterChain` – Defines HTTP security rules
- `JWTAuthFilter` – Intercepts requests, validates JWT
- `JWTUtils` – Generates, validates, and parses tokens
- `AuthenticationManager` – Authenticates login requests
- `DaoAuthenticationProvider` – Loads user data from DB

---

## What I Learn

### Spring Boot Basics

- REST APIs using `@RestController`, `@PostMapping`, `@GetMapping`, etc.
- Dependency Injection using `@Service`, `@Component`
- How to build secure REST APIs with Spring Security

### Authentication & Authorization

- Stateless security using JWT
- Build secure REST APIs with Spring Security
- Password hashing with BCrypt
- Role-based endpoint protection via `.hasAnyAuthority("ADMIN")`, etc

### Clean Architecture

- Controller → Service → Repository layering
- Separation of concerns(controller, service, repo)
- Optional, ResponseEntity, and exception handling

---

## Keywords to Remember

| Keyword                               | Purpose                              |
| ------------------------------------- | ------------------------------------ |
| `@RestController`                     | Defines REST endpoints               |
| `@Service` / `@Component`             | Marks service/logic layers           |
| `SecurityFilterChain`                 | Custom security config               |
| `JWT (JSON Web Token)`                | Stateless authentication             |
| `AuthenticationManager`               | Handles login logic                  |
| `DaoAuthenticationProvider`           | Authenticates via DB                 |
| `PasswordEncoder` (BCrypt)            | Hashes passwords                     |
| `csrf().disable()`                    | Disables CSRF for APIs               |
| `UsernamePasswordAuthenticationToken` | Auth object                          |
| `SessionCreationPolicy.STATELESS`     | Ensures JWT-based stateless sessions |
| `ResponseEntity`                      | Custom response with status          |
| `Optional<>` / `orElseThrow()`        | Null-safe DB result handling         |

---

## Postman Testing Example

1. Register:

   - `POST /auth/register`
   - Body: `{ "name": "user", "email": "user@example.com","city": "Los", "password": "123456", "role": "USER"}`

2. Login:

   - `POST /auth/login`
   - Body: `{ "email": "user@example.com", "password": "123456"}`
   - Get access token + refresh token + role

3. Use JWT in Header:

   - `Authorization: Bearer <access_token>`

4. Refresh Token:
   - `POST /auth/refresh`
   - Body: `{ "token": "<refresh_token>" }`

---

## Example API Endpoints

| Endpoint                 | Method | Description                              |
| ------------------------ | ------ | ---------------------------------------- |
| `/auth/register`         | POST   | Register a new user                      |
| `/auth/login`            | POST   | Login and receive JWT token              |
| `/auth/refresh`          | POST   | Refresh access token using refresh token |
| `/admin/get-all-users`   | GET    | Retrieve list of all users (Admin only)  |
| `/admin/get-users/{id}`  | GET    | Get user details by ID                   |
| `/admin/update/{id}`     | PUT    | Update user information                  |
| `/admin/delete/{id}`     | DELETE | Delete a user by ID                      |
| `/adminuser/get-profile` | GET    | Get logged-in user's profile             |

---

## Environment Setup

```bash
# application.properties (example)
spring.datasource.url=jdbc:mysql://localhost:3306/users_management
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.port=your_port
```
