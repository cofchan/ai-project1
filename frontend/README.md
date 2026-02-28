# User Registration System - Frontend

This is the frontend service for the user registration system built with Vue 3, Vite, and Tailwind CSS.

## Features

- **User Registration**: Register new accounts with email validation
- **Email Verification**: Verify email addresses with secure tokens
- **User Login**: Login with JWT token authentication
- **Password Recovery**: Request and reset forgotten passwords
- **OAuth2 Integration**: Social login with Google and GitHub
- **Protected Routes**: JWT-based route protection
- **State Management**: Pinia store for auth state
- **Form Validation**: Real-time form validation
- **Responsive Design**: Mobile-friendly UI with Tailwind CSS
- **Error Handling**: Comprehensive error messages and handling

## Prerequisites

- Node.js 16+ and npm/yarn
- Backend server running on http://localhost:8080

## Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Configure Environment

Create a `.env.local` file in the frontend directory:

```bash
VITE_API_URL=http://localhost:8080/api
VITE_SOCKET_URL=http://localhost:8080
```

### 3. Development Server

```bash
npm run dev
```

The frontend will be available at `http://localhost:5173`

### 4. Build for Production

```bash
npm run build
```

Output will be in the `dist` folder.

### 5. Preview Production Build

```bash
npm run preview
```

## Project Structure

```
frontend/
├── src/
│   ├── components/          # Reusable components
│   │   └── (to be created)
│   ├── stores/
│   │   ├── authStore.js     # Auth state management (Pinia)
│   │   └── userStore.js     # User state management (to be created)
│   ├── views/
│   │   ├── HomePage.vue
│   │   ├── RegisterPage.vue
│   │   ├── LoginPage.vue
│   │   ├── VerifyEmailPage.vue
│   │   ├── ForgotPasswordPage.vue
│   │   ├── ResetPasswordPage.vue
│   │   └── DashboardPage.vue
│   ├── api/
│   │   └── authApi.js       # API client for auth endpoints
│   ├── router/
│   │   └── index.js         # Vue Router configuration
│   ├── App.vue              # Root component
│   ├── main.js              # Application entry
│   └── style.css            # Global styles
├── index.html               # HTML entry point
├── package.json
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
└── README.md
```

## API Integration

The frontend communicates with the backend API using Axios. All requests include JWT token in the Authorization header.

### Endpoints Used

- `POST /auth/register` - Register new user
- `POST /auth/verify-email` - Verify email with token
- `POST /auth/login` - Login user
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password with token
- `GET /auth/profile` - Get user profile (authenticated)
- `POST /auth/logout` - Logout user

## Authentication Flow

1. **Registration**: User fills registration form → Backend validates → Email sent for verification
2. **Email Verification**: User clicks verification link → Token validated → User marked as verified
3. **Login**: User enters credentials → Backend authenticates → JWT token returned
4. **Token Storage**: Token stored in localStorage for subsequent requests
5. **Protected Routes**: Routes check authentication state and redirect if needed
6. **Password Reset**: User enters email → Token sent → User resets password with token

## State Management (Pinia)

### Auth Store (`stores/authStore.js`)

```javascript
// Access state
const auth = useAuthStore();
console.log(auth.isAuthenticated);
console.log(auth.user);

// Call actions
await auth.register(registrationData);
await auth.login(email, password);
await auth.verifyEmail(token);
await auth.forgotPassword(email);
await auth.resetPassword(token, newPassword);
auth.logout();
```

## Form Components

The following form components need to be created:

- **RegistrationForm**: Email, password, full name fields
- **LoginForm**: Email and password fields
- **PasswordResetForm**: Password reset token and new password fields
- **ForgotPasswordForm**: Email input for password reset

## Styling with Tailwind CSS

Custom components are defined in `src/style.css`:
- `.btn-primary` - Primary button style
- `.btn-secondary` - Secondary button style
- `.btn-outline` - Outline button style
- `.input-field` - Input field styling
- `.form-group` - Form group wrapper
- `.form-label` - Form label styling
- `.card` - Card container
- `.container-center` - Centered container

## Error Handling

The app includes comprehensive error handling:
- Validation error messages displayed in form
- API error messages shown to user
- 401 errors trigger redirect to login
- Network errors handled gracefully

## Routing

Protected routes require authentication:

```javascript
// Public routes
/                    # Home page
/register           # Registration page
/login              # Login page
/verify-email       # Email verification
/forgot-password    # Forgot password
/reset-password     # Reset password

// Protected routes
/dashboard          # User dashboard (requires auth)
```

## Environment Variables

Create `.env.local`:

```
VITE_API_URL=http://localhost:8080/api
VITE_SOCKET_URL=http://localhost:8080
```

## Development Tips

### Hot Module Reload (HMR)
The development server includes HMR. Changes to components are reflected instantly.

### Browser DevTools
- **Vue DevTools**: Install browser extension for Vue component inspection
- **Network Tab**: Monitor API calls and responses
- **Console**: Check for errors and warnings

### Debugging
```javascript
// Log auth state
const auth = useAuthStore();
console.log(auth.$state);

// Log API responses
import authApi from '@/api/authApi';
authApi.login({...}).then(res => console.log(res));
```

## Testing

```bash
# Run unit tests (when added)
npm run test

# Run e2e tests (when added)
npm run test:e2e
```

## Production Deployment

### Build
```bash
npm run build
```

### Deploy to Vercel
```bash
npm install -g vercel
vercel
```

### Deploy to Netlify
```bash
npm install -g netlify-cli
netlify deploy --prod --dir=dist
```

### Environment Variables in Production
Set in your hosting platform:
- `VITE_API_URL`: Backend API URL
- `VITE_SOCKET_URL`: Backend socket URL (for WebSocket, if needed)

## CORS Configuration

The backend is configured to accept requests from:
- `http://localhost:5173` (Development)
- `http://localhost:3000` (Alternative dev port)

For production, update CORS origins in backend `application.yml`

## Troubleshooting

### API Requests Failing with 403/CORS Error
- Verify backend is running on correct port
- Check backend CORS configuration
- Check browser console for detailed error

### Token Not Persisting
- Verify localStorage is enabled
- Check if token is being saved after login
- Check Application tab in DevTools

### OAuth2 Login Not Working
- Verify OAuth2 credentials are set in backend
- Check callback URLs match configuration
- Verify CORS allows OAuth routes

## Security Best Practices Implemented

1. **JWT Token Storage**: Tokens stored in localStorage (consider using secure httpOnly cookies)
2. **Token in Headers**: JWT included in Authorization header for all authenticated requests
3. **Route Protection**: Protected routes checked before rendering
4. **Form Validation**: Client-side and server-side validation
5. **CORS**: Restricted to known origins
6. **HTTPS**: Use HTTPS in production

## Performance Optimization

- **Code Splitting**: Routes are lazy-loaded
- **Tree Shaking**: Unused code is removed during build
- **Minification**: CSS and JavaScript minified in production
- **Image Optimization**: Use optimized image formats
- **Caching**: API responses can be cached strategically

## Contributing

When adding new features:
1. Create feature branch
2. Follow Vue 3 Composition API patterns
3. Add form validation with vee-validate (if needed)
4. Update error handling
5. Test with different screen sizes
6. Update documentation

## Browser Support

- Chrome/Edge: Latest 2 versions
- Firefox: Latest 2 versions
- Safari: Latest 2 versions
- iOS Safari: Latest 2 versions

## Additional Resources

- [Vue 3 Documentation](https://vuejs.org/)
- [Vite Documentation](https://vitejs.dev/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Pinia Documentation](https://pinia.vuejs.org/)
- [Axios Documentation](https://axios-http.com/)
- [Vue Router Documentation](https://router.vuejs.org/)

## License

This project is available for educational purposes.
