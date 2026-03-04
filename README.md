# User Registration System

> A full-stack user registration and authentication system with JWT, 2FA, OAuth2, and email verification. Built with Spring Boot 3.2 and Vue 3.

![Stack](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![Vue](https://img.shields.io/badge/Vue-3-blue) ![Java](https://img.shields.io/badge/Java-17-orange) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

---

## ✨ Features

- 🔐 **Authentication**: JWT-based stateless auth with BCrypt password hashing
- ✉️ **Email Verification**: Secure email verification with time-limited tokens
- 🔑 **Password Reset**: Email-based password recovery flow
- 🛡️ **Two-Factor Authentication**: Email-based 2FA (auto-enabled) + optional TOTP
- 🌐 **OAuth2 Integration**: Sign in with Google or GitHub
- 🌍 **Internationalization**: English, Español, 繁體中文
- 📧 **Email Notifications**: Login notifications, verification emails, password resets
- 🗄️ **Database**: PostgreSQL 15 with Hibernate ORM
- 🎨 **Modern UI**: Vue 3 + Vite + Tailwind CSS

---

## 📁 Project Structure

```
.
├── backend/              # Spring Boot 3.2 REST API
│   ├── src/main/java/com/registration/
│   │   ├── controller/   # REST endpoints
│   │   ├── service/      # Business logic
│   │   ├── repository/   # JPA repositories
│   │   ├── entity/       # JPA entities
│   │   ├── dto/          # Request/response DTOs
│   │   ├── exception/    # Custom exceptions
│   │   └── config/       # Security & app config
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── schema.sql
│   │   └── email/        # Email templates
│   └── pom.xml
│
├── frontend/             # Vue 3 SPA
│   ├── src/
│   │   ├── api/          # Axios API client
│   │   ├── components/   # Vue components
│   │   ├── views/        # Route pages
│   │   ├── stores/       # Pinia state management
│   │   ├── locales/      # i18n translations
│   │   └── router/       # Vue Router
│   └── package.json
│
├── docker-compose.yml    # PostgreSQL + pgAdmin
├── env.properties        # Secrets (never commit real values)
└── README.md
```

---

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Node.js 18+** and npm
- **Docker & Docker Compose** (for PostgreSQL)

### 1. Start Database

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5432) and pgAdmin (port 5050).

### 2. Configure Secrets

Create `env.properties` at the project root with your secrets:

```properties
# JWT
JWT_SECRET=your-secret-key-min-512-bits

# Email (SMTP)
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your-app-password

# OAuth2 - Google
OAUTH2_GOOGLE_CLIENT_ID=your-google-client-id
OAUTH2_GOOGLE_CLIENT_SECRET=your-google-client-secret
OAUTH2_GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/google

# OAuth2 - GitHub
OAUTH2_GITHUB_CLIENT_ID=your-github-client-id
OAUTH2_GITHUB_CLIENT_SECRET=your-github-client-secret
OAUTH2_GITHUB_REDIRECT_URI=http://localhost:8080/api/auth/oauth2/callback/github
```

**⚠️ Never commit real secrets to version control!**

### 3. Start Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend API runs at: http://localhost:8080/api

### 4. Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend dev server runs at: http://localhost:5173

---

## 📚 Documentation

- **[Backend README](backend/README.md)** — API endpoints, configuration, testing
- **[Frontend README](frontend/README.md)** — UI components, i18n, build process

---

## 🧪 Testing

### Backend Tests

```bash
cd backend
./mvnw test
```

Includes unit tests (JUnit 5 + Mockito) and integration tests (Spring Boot Test + H2).

### API Tests

```bash
cd backend/api-tests
./run_api_tests.sh
```

Bash/curl smoke tests for all endpoints (requires running backend + PostgreSQL).

---

## 🔒 Security

- **Stateless JWT**: HS512 algorithm, 24-hour expiration
- **Password Policy**: Min 8 chars, uppercase, lowercase, digit, special char
- **BCrypt**: Password hashing with strength 12
- **CSRF Protection**: Disabled (stateless API)
- **CORS**: Configured for localhost:5173 and localhost:3000
- **2FA**: Email-based (auto-enabled on verification) + optional TOTP

---

## 🌍 API Endpoints

All endpoints are prefixed with `/api/auth`:

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user | No |
| POST | `/login` | Login with credentials | No |
| POST | `/verify-email` | Verify email with token | No |
| POST | `/forgot-password` | Request password reset | No |
| POST | `/reset-password` | Reset password with token | No |
| GET | `/oauth2/authorize/{provider}` | OAuth2 authorization | No |
| GET | `/oauth2/callback/{provider}` | OAuth2 callback | No |
| POST | `/2fa/send-code` | Request 2FA code | No |
| POST | `/2fa/verify-code` | Verify 2FA code | No |
| POST | `/2fa/verify-backup-code` | Verify backup code | No |
| POST | `/2fa/setup-totp` | Setup TOTP 2FA | Yes |
| POST | `/2fa/verify-totp` | Verify TOTP setup | Yes |
| POST | `/2fa/disable-totp` | Disable TOTP 2FA | Yes |
| GET | `/profile` | Get user profile | Yes |
| POST | `/logout` | Logout user | Yes |

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.2
- **Language**: Java 17
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15 + Hibernate ORM
- **Validation**: Jakarta Bean Validation
- **Email**: JavaMailSender
- **Build**: Maven

### Frontend
- **Framework**: Vue 3 (Composition API + Options API)
- **Build Tool**: Vite
- **State Management**: Pinia
- **Routing**: Vue Router
- **HTTP Client**: Axios
- **Styling**: Tailwind CSS
- **i18n**: vue-i18n

### DevOps
- **Containerization**: Docker + Docker Compose
- **Database Admin**: pgAdmin 4

---

## 📝 License

MIT

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

---

## 📧 Support

For issues or questions, please open an issue on GitHub.
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
# User Registration System

Full-stack user registration and authentication system:

- Backend: Spring Boot 3.2 (Java 17) — stateless JWT auth, BCrypt passwords, optional OAuth2 & 2FA
- Frontend: Vue 3 + Vite — Pinia, Vue Router, Tailwind CSS, vue-i18n

This repository contains a complete dev setup (Postgres + pgAdmin) plus backend and frontend apps.

Quick start (developer):

1) Start local infrastructure (Postgres + pgAdmin):

```bash
docker-compose up -d
```

2) Run the backend:

```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

3) Run the frontend (separate terminal):

```bash
cd frontend
npm install
npm run dev
```

Notes
- The backend loads sensitive values from `env.properties` at the repository root (JWT secret, mail credentials, OAuth client secrets). Keep that file out of VCS.
- Frontend dev server proxies `/api` to `http://localhost:8080` (see `vite.config.js`).
- Local ports used: backend `8080`, frontend `5173`.

Testing
- Run backend unit and integration tests:

```bash
cd backend
mvn test
```

Where to look for more
- Backend details and commands: `backend/README.md`
- Frontend details and commands: `frontend/README.md`

If you want, I can also regenerate or expand these READMEs with more environment examples, troubleshooting, or CI snippets.
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
