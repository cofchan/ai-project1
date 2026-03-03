# Project Guidelines

## Architecture

Full-stack **User Registration System**: Spring Boot 3.2 backend (Java 17) + Vue 3 frontend.

### Backend (`backend/`)
- **Base package**: `com.registration` — controller → service → repository layering
- **Context path**: `/api`, all auth endpoints under `/auth/*` (e.g., `/api/auth/login`)
- **Security**: Stateless JWT (HS512, 24h), BCrypt(12), OAuth2 (Google/GitHub), email-based 2FA auto-enabled on verification, optional TOTP 2FA
- **Database**: PostgreSQL 15, Hibernate `ddl-auto: update`, schema in `src/main/resources/schema.sql`
- **Config**: Secrets loaded from `env.properties` at project root via `spring.config.import`

### Frontend (`frontend/`)
- **Stack**: Vue 3 + Vite + Pinia + Vue Router + Axios + Tailwind CSS + vue-i18n (en/es/zh-TW)
- **Dev proxy**: Vite proxies `/api` → `http://localhost:8080`; dev server at port 5173
- **Auth flow**: JWT in `localStorage`, Axios interceptor adds `Bearer` header, 401 → redirect to `/login`

## Code Style

### Java
- **Lombok**: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Slf4j` on all DTOs, entities, services
- **DI**: Field injection with `@Autowired` (not constructor injection)
- **Validation**: Jakarta Bean Validation on request DTOs (`@Valid`, `@NotBlank`, `@Email`, `@Size`, `@Pattern`); `@Validated` on controllers
- **Responses**: `ResponseEntity<T>` with explicit status codes; `MessageResponse` for simple success; `AuthResponse` for login; `ResponseEntity<?>` when type varies
- **DTOs**: Named `*Request` / `*Response`; separate DTO per endpoint — see [dto/](backend/src/main/java/com/registration/dto/)
- **Entities**: JPA `@PrePersist`/`@PreUpdate` for timestamps, UUID tokens via `@PrePersist` — see [entity/](backend/src/main/java/com/registration/entity/)
- **Exceptions**: Custom exceptions with `@ResponseStatus`; `GlobalExceptionHandler` returns structured `ErrorResponse`/`ValidationErrorResponse`

### Vue/JS
- **Components**: Mix of Options API and `<script setup>`; SFC order: `<template>` → `<script>` → `<style scoped>`
- **Styling**: Tailwind utility classes; custom classes (`btn`, `btn-primary`, `input-field`, `form-label`, `card`) defined in `style.css`
- **i18n**: Use `$t('key')` in templates; locales in `frontend/src/locales/`
- **Routes**: Lazy-loaded with `() => import(...)`; protected routes use `meta: { requiresAuth: true }`

## Build and Test

```bash
# Infrastructure
docker-compose up -d                    # PostgreSQL + pgAdmin

# Backend
cd backend && mvn spring-boot:run       # Run dev server (port 8080)
cd backend && mvn test                  # Unit + integration tests
cd backend && mvn clean compile         # Compile only

# Frontend
cd frontend && npm run dev              # Dev server (port 5173)
cd frontend && npm run build            # Production build
cd frontend && npm run lint             # ESLint
```

## Project Conventions

- **2FA auto-enable**: Email verification automatically enables email-based 2FA for the user — see `UserService.verifyEmail()`
- **Token trimming**: Both frontend and backend trim whitespace from verification/reset tokens before processing
- **Email templates**: Classpath resources at `resources/email/*.txt` with `{{placeholder}}` substitution
- **OAuth2 users**: Auto-verified (`isEmailVerified = true`), password nullable; created via `findOrCreateOAuth2User()`
- **Builder pattern**: Used consistently for all response construction (e.g., `MessageResponse.builder().message(...).success(true).build()`)
- **No role-based auth**: Authentication is binary; `UsernamePasswordAuthenticationToken` uses `null` authorities

## Security

- **Public endpoints**: `/auth/register`, `/auth/login`, `/auth/verify-email`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/oauth2/**`, `/auth/2fa/**`
- **Protected**: `/auth/profile` (GET), `/auth/logout` (POST), `anyRequest().authenticated()`
- **CSRF disabled** (stateless API); CORS configured for `localhost:5173` and `localhost:3000`
- **Password rules**: Min 8 chars, uppercase + lowercase + digit + special char (`@$!%*?&`)
- **Sensitive config** (`env.properties`): JWT secret, mail credentials, OAuth2 client secrets — never commit real values

## Testing Patterns

- **Unit tests**: JUnit 5 + Mockito + AssertJ; `@Mock`/`@InjectMocks`/`@Captor` — see [UserServiceTest.java](backend/src/test/java/com/registration/service/UserServiceTest.java)
- **Integration tests**: `@SpringBootTest(RANDOM_PORT)` + MockMvc + H2; mock `JavaMailSender` via `@TestConfiguration` — see [AuthControllerIntegrationTest.java](backend/src/test/java/com/registration/controller/AuthControllerIntegrationTest.java)
- **API smoke tests**: Bash/curl in `backend/api-tests/run_api_tests.sh` (requires running PostgreSQL)
