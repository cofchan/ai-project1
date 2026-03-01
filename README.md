# User Registration System

A full-stack user registration system with email verification, password reset, and OAuth2 authentication.

**Technology Stack:**
- **Backend**: Spring Boot 3.2, Java 17, PostgreSQL
- **Frontend**: Vue 3, Vite, Tailwind CSS, Pinia
- **Database**: PostgreSQL 15
- **Authentication**: JWT + Spring Security + OAuth2

## Quick Start

### Prerequisites
- Java 17+
- Node.js 16+
- PostgreSQL 12+ (or Docker + Docker Compose)
- Maven 3.6+
- npm or yarn

### 1. Setup Database

Using Docker Compose (Recommended):
```bash
docker-compose up -d
```

This starts PostgreSQL on port 5432 and pgAdmin on port 5050.

### 2. Configure Environment

Copy the example env file and configure:
```bash
cp .env.example .env
```

Edit `.env` with your settings:
- Email service credentials (Gmail, SendGrid, etc.)
- OAuth2 credentials (Google, GitHub)
- JWT secret

### 3. Start Backend

```bash
cd backend

# Build
mvn clean install

# Run
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

### 4. Start Frontend

In a new terminal:
```bash
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev
```

Frontend runs on: `http://localhost:5173`

## Project Structure

```
user-registration-system/
├── backend/                  # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/registration/
│   │   │   │   ├── entity/            # JPA Entities
│   │   │   │   ├── repository/        # Data repositories
│   │   │   │   ├── service/           # Business logic
│   │   │   │   ├── controller/        # REST endpoints
│   │   │   │   ├── dto/               # Data transfer objects
│   │   │   │   ├── exception/         # Custom exceptions
│   │   │   │   └── config/            # Configuration classes
│   │   │   └── resources/
│   │   │       ├── application.yml    # Spring config
│   │   │       └── schema.sql         # Database schema
│   │   └── test/                      # Unit tests
│   ├── pom.xml                        # Maven dependencies
│   └── README.md
│
├── frontend/                 # Vue 3 application
│   ├── src/
│   │   ├── components/      # Vue components
│   │   ├── stores/          # Pinia stores
│   │   ├── views/           # Page components
│   │   ├── router/          # Vue Router config
│   │   ├── api/             # API client
│   │   ├── App.vue          # Root component
│   │   ├── main.js          # Entry point
│   │   └── style.css        # Global styles
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   └── README.md
│
├── docker-compose.yml       # Database setup
├── .env.example             # Environment variables template
├── .gitignore
└── README.md               # This file
```

## Features Implemented

### Backend (Spring Boot)
✅ User registration with validation
✅ Email verification with time-limited tokens (24 hours)
✅ User login with JWT authentication
✅ Password reset with time-limited tokens (1 hour)
✅ Spring Security configuration
✅ CORS configuration
✅ Global exception handling
✅ Request validation
✅ Comprehensive logging
✅ Two-factor authentication via email code sent after login

### Frontend (Vue 3)
✅ Project structure and routing
✅ Pinia store for auth state management
✅ API client with Axios and interceptors
✅ Form layouts (ready for form fields)
✅ Tailwind CSS styling
✅ Navigation with auth checks
✅ Error handling

## API Endpoints

### Register
```bash
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "passwordConfirm": "SecurePass123!",
  "fullName": "John Doe"
}
```

### Verify Email
```bash
POST /api/auth/verify-email
{
  "token": "uuid-token"
}
```

> **Note:** once the token is accepted the account is marked verified and two-factor authentication is enabled by default. The first login after verification will therefore require a 6‑digit email code (unless the user later configures a different 2FA method).

### Login
```bash
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

If the account has two-factor authentication enabled the response will return `requiresTwoFA=true`, a message indicating a code was sent, and the JWT token will be omitted. The client must then submit the 6‑digit code via:

```bash
POST /api/auth/2fa/verify
{
  "email": "user@example.com",
  "code": "123456"
}
```
Frontend clients should provide a smooth experience when the code is required:
* automatically focus the verification field when it appears
* allow the user to resend the code (re‑triggering the login endpoint with the same credentials)
* include a "Back to login" link so the user can re‑enter credentials or correct mistakes
* show helpful validation and error messages as the user types

These enhancements are implemented in the provided `frontend/src/components/LoginForm.vue` component.### Forgot Password
```bash
POST /api/auth/forgot-password
{
  "email": "user@example.com"
}
```

### Reset Password
```bash
POST /api/auth/reset-password
{
  "token": "uuid-token",
  "newPassword": "NewSecurePass123!"
}
```

## Development Workflow

### Backend Development
1. Create feature branch
2. Make changes to service layer
3. Update DTOs if needed
4. Test endpoints with curl or Postman
5. Check logs for errors

### Frontend Development
1. Create feature branch
2. Update store actions if needed
3. Build components and forms
4. Test with live dev server
5. Use Vue DevTools for debugging

### Common Commands

```bash
# Backend
cd backend
mvn clean install        # Build
mvn spring-boot:run     # Run dev
mvn test                # Run tests
mvn package             # Package JAR

# Frontend
cd frontend
npm install             # Install deps
npm run dev            # Dev server
npm run build          # Production build
npm run preview        # Preview build
npm run lint           # Lint code
```

## Configuration

### Backend Configuration (`backend/application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/registration_db
    username: postgres
    password: postgres

  mail:
    host: smtp.gmail.com
    username: your-email@gmail.com
    password: app-specific-password

jwt:
  secret: your-256-bit-secret-key
  expiration: 86400000  # 24 hours

app:
  email-verification-expiry-hours: 24
  password-reset-expiry-minutes: 60
  frontend-url: http://localhost:5173
```

### Frontend Configuration (`.env.local`)

```
VITE_API_URL=http://localhost:8080/api
VITE_SOCKET_URL=http://localhost:8080
```

## Database Schema

### Users Table
- id (PK)
- email (UNIQUE)
- password (hashed)
- full_name
- is_email_verified
- oauth2_provider
- oauth2_id
- created_at
- updated_at

### Email Verification Tokens
- id (PK)
- user_id (FK)
- token (UNIQUE)
- expiry_time
- created_at

### Password Reset Tokens
- id (PK)
- user_id (FK)
- token (UNIQUE)
- expiry_time
- used
- created_at

## Security Features

✅ Passwords hashed with BCrypt (strength 12)
✅ JWT tokens signed with HS512
✅ Email and password reset tokens are single-use
✅ Tokens have expiry times
✅ CORS configured for trusted origins
✅ CSRF protection disabled for stateless API
✅ Input validation (client + server)
✅ Comprehensive logging and auditing
✅ Spring Security for authorization

## Password Requirements

Passwords must meet:
- Minimum 8 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@, $, !, %, *, ?, &)

Example: `SecurePass123!`

## Troubleshooting

### Database Connection Issues
1. Ensure PostgreSQL is running
2. Check credentials in `application.yml`
3. Verify database exists: `psql -U postgres -l`

### Email Not Sending
1. Check MAIL_USERNAME and MAIL_PASSWORD
2. Gmail requires app-specific password (not account password)
3. Check application logs for SMTP errors

### CORS Errors
1. Verify frontend URL in backend CORS config
2. Check backend is running on correct port
3. Verify API endpoints are correct

### Token Issues
1. Check JWT_SECRET is set
2. Verify Authorization header format: `Bearer <token>`
3. Ensure token hasn't expired

## Environment Variables

Create `.env` file in root directory with your configuration. See `.env.example` for template.

## Production Deployment

### Backend
1. Build: `mvn clean package`
2. Deploy JAR to server
3. Configure environment variables
4. Use PostgreSQL managed service
5. Configure health checks
6. Enable HTTPS
7. Set strong JWT secret

### Frontend
1. Build: `npm run build`
2. Deploy `dist` folder to CDN or static hosting
3. Configure backend API URL
4. Enable HTTPS
5. Set cache headers

## Performance Tips

- Backend: Use connection pooling, enable query caching
- Frontend: Lazy load routes, use code splitting
- Database: Add indexes on foreign keys and frequently searched columns
- API: Implement request rate limiting

## Next Steps / TODO

- [ ] OAuth2 full integration (Google, GitHub)
- [ ] Complete Vue form components with validation
- [ ] Unit tests for backend services
- [ ] Integration tests
- [ ] E2E tests with Cypress/Playwright
- [ ] Admin dashboard
- [ ] User profile management
- [ ] Rate limiting
- [ ] Database encryption
- [ ] Brute force protection
- [ ] Two-factor authentication
- [ ] WebSocket for real-time notifications

## Contributing

1. Create feature branch: `git checkout -b feature/xyz`
2. Make changes and commit: `git commit -m "Add xyz"`
3. Push to branch: `git push origin feature/xyz`
4. Open pull request

## Resources

**Backend Documentation:**
- [Spring Boot 3.2](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

**Frontend Documentation:**
- [Vue 3](https://vuejs.org/)
- [Vite](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Pinia](https://pinia.vuejs.org/)
- [Axios](https://axios-http.com/)

**Infrastructure:**
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/)
- [JWT.io](https://jwt.io/)

## License

This project is available for educational purposes.

## Support

For detailed documentation:
- Backend: See [backend/README.md](backend/README.md)
- Frontend: See [frontend/README.md](frontend/README.md)

---

**Created**: February 2026
**Last Updated**: February 19, 2026
    │   └── style.css
    ├── index.html
    ├── vite.config.js
    ├── tailwind.config.js
    ├── postcss.config.js
    └── package.json
```

## Prerequisites

- **Java**: 17 or higher
- **Node.js**: 16 or higher
- **npm**: 9 or higher
- **PostgreSQL**: 12 or higher
- **Maven**: 3.8 or higher

## Installation & Setup

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE registration_db;
```

Update credentials in `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/registration_db
    username: postgres
    password: your_password
```

### 2. Backend Setup

```bash
cd backend

# Install dependencies
mvn clean install

# Run the application
mvn spring-boot:run

# Application will start on http://localhost:8080
```

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev

# Application will start on http://localhost:5173
```

## Configuration

### Environment Variables

Create a `.env` file in the project root:

```bash
# Email Configuration
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password

# JWT
JWT_SECRET=your-secret-key-at-least-256-bits

# OAuth2 - Google
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# OAuth2 - GitHub
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
```

### JWT Configuration

Update `application.yml`:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/verify-email` - Verify email address
- `POST /api/auth/forgot-password` - Request password reset
- `POST /api/auth/reset-password` - Reset password with token
- `GET /api/auth/oauth2/google` - Google OAuth2 login
- `GET /api/auth/oauth2/github` - GitHub OAuth2 login
- `GET /api/auth/oauth2/callback` - OAuth2 callback handler

## Frontend Pages

- `/` - Home page
- `/register` - User registration
- `/login` - User login
- `/verify-email` - Email verification
- `/forgot-password` - Password reset request
- `/reset-password` - Password reset form
- `/dashboard` - User dashboard (protected)

## Development

### Running Tests

**Backend:**
```bash
cd backend
mvn test
```

**Frontend:**
```bash
cd frontend
npm run test
```

### Building for Production

**Backend:**
```bash
cd backend
mvn clean package
```

**Frontend:**
```bash
cd frontend
npm run build
```

## Security Features

- ✅ Password encryption with BCrypt (strength 12)
- ✅ Email verification with UUID tokens (24-hour expiry)
- ✅ Password reset with secure tokens (1-hour expiry)
- ✅ OAuth2 integration (Google, GitHub)
- ✅ JWT token authentication
- ✅ CORS configuration
- ✅ SQL injection prevention (parameterized queries)
- ✅ XSS protection

## Database Schema

### users
- id: BIGSERIAL PRIMARY KEY
- email: VARCHAR(255) UNIQUE NOT NULL
- password: VARCHAR(255)
- full_name: VARCHAR(255)
- is_email_verified: BOOLEAN (default: false)
- oauth2_provider: VARCHAR(50)
- oauth2_id: VARCHAR(255)
- created_at: TIMESTAMP
- updated_at: TIMESTAMP

### email_verification_tokens
- id: BIGSERIAL PRIMARY KEY
- user_id: BIGINT FOREIGN KEY
- token: VARCHAR(255) UNIQUE NOT NULL
- expiry_time: TIMESTAMP NOT NULL
- created_at: TIMESTAMP

### password_reset_tokens
- id: BIGSERIAL PRIMARY KEY
- user_id: BIGINT FOREIGN KEY
- token: VARCHAR(255) UNIQUE NOT NULL
- expiry_time: TIMESTAMP NOT NULL
- used: BOOLEAN (default: false)
- created_at: TIMESTAMP

## Future Enhancements

- [ ] Two-factor authentication (2FA)
- [ ] Additional OAuth2 providers (Facebook, LinkedIn, Apple)
- [ ] JWT refresh token rotation
- [ ] User profile management
- [ ] Account deletion/deactivation
- [ ] Brute force protection
- [ ] Login history and device tracking
- [ ] Email change verification
- [ ] Phone number verification
- [ ] Admin dashboard
- [ ] User activity logging

## Troubleshooting

### Database Connection Issues
- Ensure PostgreSQL is running
- Verify database name, username, and password in `application.yml`
- Check PostgreSQL port (default: 5432)

### CORS Errors
- Check `application.yml` for allowed origins
- Verify frontend URL is in allowed list

### Email Issues
- Enable "Less secure app access" for Gmail
- Use Gmail App Passwords for 2FA protected accounts
- Check SMTP configuration in `application.yml`

### OAuth2 Issues
- Verify client IDs and secrets are correct
- Ensure redirect URIs match exactly in OAuth2 provider settings
- Check environment variables are loaded

## License

MIT License - See LICENSE file for details

## Support

For issues and questions, please create an issue in the repository.
