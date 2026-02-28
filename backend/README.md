# User Registration System - Backend

This is the backend service for the user registration system built with Spring Boot 3.x and Java 17.

## Features

- **User Registration**: Register new users with email and password
- **Email Verification**: Secure email verification with time-limited tokens (24 hours)
- **Login**: Authenticate users with JWT tokens
- **Password Reset**: Secure password reset with time-limited tokens (1 hour)
- **Spring Security**: JWT-based authentication and authorization
- **CORS**: Configured for frontend communication
- **Input Validation**: Comprehensive validation using Jakarta Validation
- **Error Handling**: Global exception handler with detailed error responses
- **Logging**: SLF4J logging with detailed audit trails

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- (Optional) Docker and Docker Compose

## Setup

### 1. Database Setup

#### Option A: Using Docker Compose (Recommended)

```bash
cd /home/cofchan/ai-code/ai-project1
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- pgAdmin on port 5050 (admin@pgadmin.com / admin)

#### Option B: Manual PostgreSQL Setup

```bash
# Create database
psql -U postgres -c "CREATE DATABASE registration_db;"

# Run schema.sql
psql -U postgres -d registration_db -f backend/src/main/resources/schema.sql
```

### 2. Configure Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
cp .env.example .env
```

Edit `.env` with:
- `MAIL_USERNAME`: Your email for sending verification/reset emails
- `MAIL_PASSWORD`: Your email app-specific password
- `GOOGLE_CLIENT_ID`: Google OAuth2 client ID
- `GOOGLE_CLIENT_SECRET`: Google OAuth2 client secret
- `GITHUB_CLIENT_ID`: GitHub OAuth2 client ID
- `GITHUB_CLIENT_SECRET`: GitHub OAuth2 client secret
- `JWT_SECRET`: A secure random string (min 256 bits)

### 3. Build the Project

```bash
cd backend
mvn clean install
```

### 4. Run the Application

```bash
# Development mode
mvn spring-boot:run

# Or package and run JAR
mvn clean package
java -jar target/user-registration-system-1.0.0.jar
```

The backend will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

#### Register User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "passwordConfirm": "SecurePass123!",
  "fullName": "John Doe"
}

Response (201 Created):
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "isEmailVerified": false,
  "message": "User registered successfully. Please verify your email."
}
```

#### Verify Email
```bash
POST /api/auth/verify-email
Content-Type: application/json

{
  "token": "uuid-verification-token"
}

Response (200 OK):
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "isEmailVerified": true,
  "oauth2Provider": null
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
  "type": "Bearer",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe",
    "isEmailVerified": true,
    "oauth2Provider": null
  },
  "message": "Authentication successful"
}
```

#### Forgot Password
```bash
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response (200 OK):
{
  "message": "Password reset email has been sent to user@example.com",
  "success": true
}
```

#### Reset Password
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
Authorization: Bearer <jwt-token>

Response (200 OK):
{
  "id": 1,
  "email": "user@example.com",
  "fullName": "John Doe",
  "isEmailVerified": true,
  "oauth2Provider": null
}
```

## Password Requirements

Passwords must meet the following requirements:
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@, $, !, %, *, ?, &)

Example: `SecurePass123!`

## Project Structure

```
backend/
├── src/main/java/com/registration/
│   ├── entity/
│   │   ├── User.java
│   │   ├── EmailVerificationToken.java
│   │   └── PasswordResetToken.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── EmailVerificationTokenRepository.java
│   │   └── PasswordResetTokenRepository.java
│   ├── service/
│   │   ├── UserService.java
│   │   └── EmailService.java
│   ├── controller/
│   │   └── AuthController.java
│   ├── dto/
│   │   ├── RegistrationRequest.java
│   │   ├── LoginRequest.java
│   │   ├── UserResponse.java
│   │   ├── AuthResponse.java
│   │   ├── VerifyEmailRequest.java
│   │   ├── ForgotPasswordRequest.java
│   │   ├── ResetPasswordRequest.java
│   │   └── MessageResponse.java
│   ├── exception/
│   │   ├── UserAlreadyExistsException.java
│   │   ├── InvalidTokenException.java
│   │   ├── UserNotFoundException.java
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   └── ValidationErrorResponse.java
│   └── config/
│       ├── SecurityConfig.java
│       ├── JwtTokenProvider.java
│       ├── JwtAuthenticationFilter.java
│       └── JwtAuthenticationEntryPoint.java
├── src/main/resources/
│   ├── application.yml
│   └── schema.sql
├── src/test/java/com/registration/
└── pom.xml
```

## Configuration

### Email Configuration

The application uses Gmail SMTP by default. To enable email:

1. Create a Gmail account
2. Enable 2-factor authentication
3. Generate an app-specific password
4. Set environment variables:
   - `MAIL_USERNAME`: your-email@gmail.com
   - `MAIL_PASSWORD`: generated-app-password

### JWT Configuration

JWT tokens expire after 24 hours. Configure in `application.yml`:
- `jwt.secret`: 256-bit secret key
- `jwt.expiration`: Token expiration time in milliseconds (default: 86400000 = 24 hours)

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
