# Backend — Spring Boot REST API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![Java](https://img.shields.io/badge/Java-17-orange) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

> REST API for user registration, authentication, 2FA, OAuth2, and email notifications. Stateless JWT security with Spring Security, PostgreSQL persistence, and comprehensive email workflows.

---

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+** (or use included `./mvnw`)
- **PostgreSQL 15** (via Docker Compose recommended)

### 1. Start Database

From project root:

```bash
docker-compose up -d
```

### 2. Configure Secrets

Create `env.properties` at the **project root** (one level up from `backend/`):

```properties
# JWT Configuration
JWT_SECRET=your-secret-key-must-be-at-least-512-bits-long

# Email Configuration (SMTP)
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-app-specific-password
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587

# OAuth2 - Google
OAUTH2_GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
OAUTH2_GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/google

# OAuth2 - GitHub
OAUTH2_GITHUB_CLIENT_ID=your-github-client-id
OAUTH2_GITHUB_CLIENT_SECRET=your-github-client-secret
OAUTH2_GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/github
```

**⚠️ Security Warning**: Never commit `env.properties` with real credentials!

### 3. Run the Application

```bash
cd backend
./mvnw spring-boot:run
```

API runs at: **http://localhost:8080/api**

### 4. Verify Setup

Test the health of the API:

```bash
curl http://localhost:8080/api/auth/register
```

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

Tests include:
- **Unit Tests**: JUnit 5 + Mockito + AssertJ
- **Integration Tests**: `@SpringBootTest` + MockMvc + H2 in-memory DB

### Run Specific Test Class

```bash
./mvnw test -Dtest=UserServiceTest
./mvnw test -Dtest=AuthControllerIntegrationTest
```

### API Smoke Tests

Manual API tests using curl:

```bash
cd api-tests
./run_api_tests.sh
```

---

## 📋 API Endpoints

Base URL: `/api/auth`

### Public Endpoints (No Authentication Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login with email/password |
| POST | `/verify-email` | Verify email with token |
| POST | `/forgot-password` | Request password reset email |
| POST | `/reset-password` | Reset password with token |
| GET | `/oauth2/authorize/{provider}` | Initiate OAuth2 flow (google/github) |
| GET | `/oauth2/callback/{provider}` | OAuth2 callback handler |
| POST | `/2fa/send-code` | Request 2FA email code |
| POST | `/2fa/verify-code` | Verify 2FA email code |
| POST | `/2fa/verify-backup-code` | Verify 2FA backup code |

### Protected Endpoints (JWT Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/profile` | Get current user profile |
| POST | `/logout` | Logout (client-side JWT removal) |
| POST | `/2fa/setup-totp` | Setup TOTP 2FA |
| POST | `/2fa/verify-totp` | Confirm TOTP setup |
| POST | `/2fa/disable-totp` | Disable TOTP 2FA |

### Example Requests

#### Register User

```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe"
}

Response (200 OK):
{
  "message": "Registration successful. Please check your email to verify your account.",
  "success": true
}
```

#### Login

```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get Profile (Authenticated)

```bash
GET /api/auth/profile
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...

Response (200 OK):
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "emailVerified": true,
  "twoFactorEnabled": true,
  "totpEnabled": false
}
```

---

## 🏗️ Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/registration/
│   │   │   ├── UserRegistrationApplication.java   # Main application
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java            # Spring Security setup
│   │   │   │   ├── JwtAuthenticationFilter.java   # JWT filter
│   │   │   │   └── JwtTokenProvider.java          # JWT generation/validation
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java            # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── UserService.java               # User business logic
│   │   │   │   ├── EmailService.java              # Email sending
│   │   │   │   ├── TwoFactorService.java          # 2FA logic
│   │   │   │   └── OAuth2Service.java             # OAuth2 integration
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java            # JPA repository
│   │   │   ├── entity/
│   │   │   │   └── User.java                      # JPA entity
│   │   │   ├── dto/
│   │   │   │   ├── *Request.java                  # Request DTOs
│   │   │   │   └── *Response.java                 # Response DTOs
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java    # Exception handling
│   │   │       └── *Exception.java                # Custom exceptions
│   │   └── resources/
│   │       ├── application.yml                    # App configuration
│   │       ├── schema.sql                         # Database schema
│   │       └── email/
│   │           ├── verification_email.txt
│   │           ├── password_reset_email.txt
│   │           └── login_notification_email.txt
│   └── test/
│       └── java/com/registration/
│           ├── controller/
│           │   └── AuthControllerIntegrationTest.java
│           └── service/
│               └── UserServiceTest.java
├── api-tests/
│   └── run_api_tests.sh                           # Bash API tests
├── pom.xml                                        # Maven dependencies
└── README.md
```

---

## ⚙️ Configuration

### Application Configuration

`src/main/resources/application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: user-registration-system
  datasource:
    url: jdbc:postgresql://localhost:5432/user_registration
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
  config:
    import: optional:file:../env.properties
```

### Secret Management

Secrets are loaded from `env.properties` at the project root via `spring.config.import`. The application expects the following properties:

- `JWT_SECRET` — Secret key for JWT signing (min 512 bits)
- `MAIL_USERNAME` — SMTP username
- `MAIL_PASSWORD` — SMTP password
- `OAUTH2_GOOGLE_CLIENT_ID` — Google OAuth2 client ID
- `OAUTH2_GOOGLE_CLIENT_SECRET` — Google OAuth2 secret
- `OAUTH2_GITHUB_CLIENT_ID` — GitHub OAuth2 client ID
- `OAUTH2_GITHUB_CLIENT_SECRET` — GitHub OAuth2 secret

### Database Schema

`src/main/resources/schema.sql` contains the initial database structure:

- **users** table: id, email, password, first_name, last_name, email_verified, verification_token, etc.

Schema is auto-applied via Hibernate `ddl-auto: update`.

---

## 🔒 Security

### Authentication Flow

1. User registers → email verification token sent
2. User verifies email → email-based 2FA auto-enabled
3. User logs in → if 2FA enabled, email code required
4. After successful 2FA → JWT token issued (24-hour expiry)
5. Client stores JWT → includes in `Authorization: Bearer <token>` header

### Password Requirements

Validated via `@Pattern` annotation on DTOs:

- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit
- At least one special character (`@$!%*?&`)

Example valid password: `SecurePass123!`

### JWT Details

- **Algorithm**: HS512
- **Expiration**: 24 hours
- **Claims**: email, subject (email)
- **Header**: `Authorization: Bearer <token>`

### 2FA Features

- **Email 2FA**: Auto-enabled after email verification; 6-digit codes valid for 10 minutes
- **TOTP 2FA**: Optional; Google Authenticator compatible; 10 backup codes generated
- **Backup Codes**: Single-use codes for account recovery

---

## 📦 Key Dependencies

- **spring-boot-starter-web**: REST API framework
- **spring-boot-starter-security**: Authentication & authorization
- **spring-boot-starter-data-jpa**: Database ORM
- **postgresql**: PostgreSQL JDBC driver
- **jjwt**: JWT generation & validation
- **lombok**: Boilerplate code reduction
- **spring-boot-starter-mail**: Email sending
- **spring-boot-starter-validation**: Bean validation
- **spring-boot-starter-test**: Testing framework

---

## 🐛 Troubleshooting

### Database Connection Issues

**Symptom**: `Connection refused` or `database does not exist`

**Solutions**:
1. Verify PostgreSQL is running: `docker-compose ps`
2. Check connection details in `application.yml`
3. Manually create database: `docker exec -it user_registration_postgres psql -U postgres -c "CREATE DATABASE user_registration;"`

### Emails Not Sending

**Symptom**: `AuthenticationFailedException` or timeout

**Solutions**:
1. Verify SMTP credentials in `env.properties`
2. Enable "Less secure app access" or use app-specific passwords (Gmail)
3. Check firewall/network access to SMTP server
4. Test with a different SMTP provider (e.g., Mailtrap for dev)

### JWT Token Invalid

**Symptom**: `401 Unauthorized` on protected endpoints

**Solutions**:
1. Verify token is included in `Authorization: Bearer <token>` header
2. Check token hasn't expired (24-hour lifetime)
3. Ensure `JWT_SECRET` matches between token generation and validation
4. Verify token format: `eyJ...` (Base64 encoded JWT)

### OAuth2 Redirect Mismatch

**Symptom**: `redirect_uri_mismatch` error from OAuth provider

**Solutions**:
1. Verify redirect URI in `env.properties` matches OAuth2 provider console
2. Ensure URI is exactly: `http://localhost:8080/api/auth/oauth2/callback/{provider}`
3. Re-save settings in Google/GitHub OAuth console

### Build Failures

**Symptom**: `mvn` command not found or compilation errors

**Solutions**:
1. Use included Maven wrapper: `./mvnw` instead of `mvn`
2. Verify Java 17 is installed: `java -version`
3. Clean and rebuild: `./mvnw clean compile`

---

## 🧰 Development Tips

### Lombok Setup

This project uses Lombok to reduce boilerplate. Ensure your IDE has Lombok plugin installed:

- **IntelliJ IDEA**: Install Lombok plugin + Enable annotation processing
- **Eclipse**: Run `java -jar lombok.jar` installer
- **VS Code**: Install "Lombok Annotations Support" extension

### Hot Reload

Spring Boot DevTools is included for automatic restarts during development. Simply save a file and the app will restart automatically.

### Database GUI

pgAdmin 4 is available via Docker Compose at http://localhost:5050:

- **Email**: admin@admin.com
- **Password**: admin

Add server connection:
- **Host**: postgres (or localhost if outside Docker)
- **Port**: 5432
- **Username**: postgres
- **Password**: postgres
- **Database**: user_registration

---

## 📝 Code Conventions

### Lombok Annotations

All DTOs, entities, and services use:

- `@Data` — getters, setters, toString, equals, hashCode
- `@Builder` — builder pattern
- `@NoArgsConstructor`, `@AllArgsConstructor` — constructors
- `@Slf4j` — logging

### Dependency Injection

Field injection with `@Autowired`:

```java
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
}
```

### Response Patterns

- Success: `ResponseEntity.ok(MessageResponse.builder().message(...).success(true).build())`
- Error: Custom `@ResponseStatus` exceptions caught by `GlobalExceptionHandler`
- DTOs: Separate `*Request` and `*Response` classes for each endpoint

---

## 📄 License

MIT
> **Note:** the token value is trimmed before verification, so extra spaces are ignored.

```bash
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "uuid-reset-token",
  "newPassword": "NewSecurePass123!"
}

Response (200 OK):
{
  "message": "Password has been reset successfully",
  "success": true
}
```

#### Get Profile (Requires Authentication)
```bash
GET /api/auth/profile
<img src="https://img.shields.io/badge/backend-Spring%20Boot%203.2-green" alt="Spring Boot" />

# Backend — Spring Boot API

REST API for registration, authentication, 2FA, OAuth2, and email notifications. Stateless JWT security, PostgreSQL 15, Hibernate ORM.

---

## Setup

### Prerequisites
- Java 17
- PostgreSQL 15 (via Docker Compose)

### 1. Configure Secrets
Create `../env.properties` at project root:
```
JWT_SECRET=your_jwt_secret
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_password
OAUTH2_GOOGLE_CLIENT_ID=...
OAUTH2_GOOGLE_CLIENT_SECRET=...
# ...see schema in this file
```

### 2. Start Database
```bash
- `jwt.expiration`: Token expiration time in milliseconds (default: 86400000 = 24 hours)
```

### 3. Run Backend
```bash
./mvnw spring-boot:run
```
API: [http://localhost:8080/api](http://localhost:8080/api)

---

## Configuration

- `application.yml`: Main config
- `env.properties`: Secrets (imported)
- `schema.sql`: DB schema

---

## API Endpoints

- `/api/auth/register` — Register
- `/api/auth/login` — Login
- `/api/auth/verify-email` — Email verification
- `/api/auth/forgot-password` — Request reset
- `/api/auth/reset-password` — Reset password
- `/api/auth/oauth2/**` — OAuth2
- `/api/auth/2fa/**` — 2FA
- `/api/auth/profile` — Get profile (JWT)
- `/api/auth/logout` — Logout

See controller JavaDocs for details.

---

## Testing

```bash
./mvnw test
```
Unit: JUnit 5, Mockito, AssertJ  
Integration: @SpringBootTest, MockMvc, H2

---

## Troubleshooting

- **DB connection error**: Check Docker Compose, `env.properties`, and `application.yml`.
- **Email not sent**: Check SMTP config in `env.properties`.
- **OAuth2**: Ensure client secrets are set.

---

## Key Packages

- `controller/` — REST endpoints
- `service/` — Business logic
- `repository/` — JPA
- `dto/` — Request/response models
- `entity/` — JPA entities
- `exception/` — Custom exceptions

---

## License

MIT

## Security Considerations

- All passwords are hashed using BCrypt with strength 12
- JWT tokens are signed with HS512 algorithm
- Email and password reset tokens are single-use and time-limited
- CORS is configured to only allow requests from known origins
- CSRF protection is disabled for stateless API
- All sensitive operations are logged for auditing

## Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run tests with coverage
mvn jacoco:report
```

## Troubleshooting

### Connection to Database Failed
- Ensure PostgreSQL is running
- Check connection settings in `application.yml`
- Verify database credentials

### Email Not Sending
- Check `MAIL_USERNAME` and `MAIL_PASSWORD`
- Ensure Gmail app-specific password is set correctly
- Check application logs for SMTP errors

### JWT Token Invalid
- Verify `JWT_SECRET` is set correctly
- Check token is being sent in Authorization header as: `Bearer <token>`
- Verify token hasn't expired (24 hours)

## Production Deployment

For production deployment:

1. Change `spring.jpa.hibernate.ddl-auto` from `update` to `validate`
2. Store sensitive config in environment variables
3. Use a managed PostgreSQL instance
4. Configure a real email service (SendGrid, AWS SES, etc.)
5. Enable HTTPS
6. Configure proper CORS origins
7. Set strong JWT secret
8. Enable logging and monitoring

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT Introduction](https://jwt.io/)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

## License

This project is available for educational purposes.
