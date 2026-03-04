# Frontend — Vue 3 SPA

![Vue](https://img.shields.io/badge/Vue-3-blue) ![Vite](https://img.shields.io/badge/Vite-5-purple) ![Tailwind](https://img.shields.io/badge/Tailwind-3-cyan)

> Vue 3 single-page application for user registration, authentication, 2FA, and OAuth2. Features Pinia state management, vue-i18n internationalization, and Tailwind CSS styling.

---

## 🚀 Quick Start

### Prerequisites

- **Node.js 18+** and npm

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Start Development Server

```bash
npm run dev
```

App runs at: **http://localhost:5173**

The dev server automatically proxies `/api` requests to `http://localhost:8080` (backend).

---

## 🏗️ Build & Deploy

### Production Build

```bash
npm run build
```

Optimized build output in `dist/` directory.

### Preview Production Build

```bash
npm run preview
```

Serves the production build locally for testing.

### Lint Code

```bash
npm run lint
```

---

## 🌍 Internationalization (i18n)

The app supports three languages:

- 🇬🇧 **English** (`en`)
- 🇪🇸 **Español** (`es`)
- 🇹🇼 **繁體中文** (`zh-TW`)

### Locale Files

Translation files are located in `src/locales/`:

- `en.json` — English translations
- `es.json` — Spanish translations
- `zh-TW.json` — Traditional Chinese translations

### Using Translations in Components

In templates:

```vue
<template>
  <h1>{{ $t('welcome.title') }}</h1>
  <p>{{ $t('welcome.description') }}</p>
</template>
```

In script (Composition API):

```vue
<script setup>
import { useI18n } from 'vue-i18n';

const { t, locale } = useI18n();

const title = t('welcome.title');
locale.value = 'es'; // Switch to Spanish
</script>
```

In script (Options API):

```vue
<script>
export default {
  computed: {
    welcomeMessage() {
      return this.$t('welcome.title');
    }
  },
  methods: {
    changeLanguage(lang) {
      this.$i18n.locale = lang;
    }
  }
}
</script>
```

### Adding New Translations

1. Add the key to all locale files (`en.json`, `es.json`, `zh-TW.json`)
2. Use the key in your component with `$t('your.new.key')`

Example:

```json
// en.json
{
  "forms": {
    "submit": "Submit"
  }
}

// es.json
{
  "forms": {
    "submit": "Enviar"
  }
}

// zh-TW.json
{
  "forms": {
    "submit": "提交"
  }
}
```

---

## 📁 Project Structure

```
frontend/
├── src/
│   ├── main.js                    # App entry point
│   ├── App.vue                    # Root component
│   ├── style.css                  # Global styles (Tailwind + custom)
│   ├── i18n.js                    # i18n configuration
│   │
│   ├── api/
│   │   └── authApi.js             # Axios API client
│   │
│   ├── components/
│   │   ├── LoginForm.vue          # Login form
│   │   ├── RegistrationForm.vue   # Registration form
│   │   ├── ForgotPasswordForm.vue # Password reset request
│   │   ├── ResetPasswordForm.vue  # Password reset
│   │   ├── VerifyEmailForm.vue    # Email verification
│   │   └── TwoFASetupForm.vue     # 2FA setup
│   │
│   ├── views/
│   │   ├── HomePage.vue           # Landing page
│   │   ├── LoginPage.vue          # Login page
│   │   ├── RegisterPage.vue       # Registration page
│   │   ├── DashboardPage.vue      # User dashboard (protected)
│   │   ├── ForgotPasswordPage.vue # Forgot password page
│   │   ├── ResetPasswordPage.vue  # Reset password page
│   │   └── VerifyEmailPage.vue    # Email verification page
│   │
│   ├── router/
│   │   └── index.js               # Vue Router config
│   │
│   ├── stores/
│   │   └── authStore.js           # Pinia auth store
│   │
│   └── locales/
│       ├── en.json                # English translations
│       ├── es.json                # Spanish translations
│       └── zh-TW.json             # Traditional Chinese translations
│
├── index.html                     # HTML template
├── vite.config.js                 # Vite configuration
├── tailwind.config.js             # Tailwind CSS config
├── postcss.config.js              # PostCSS config
├── package.json                   # npm dependencies
└── README.md
```

---

## 🔌 API Integration

### Axios Configuration

`src/api/authApi.js` contains the Axios client with:

- Base URL: `http://localhost:8080/api`
- Auto-injection of JWT from `localStorage`
- Automatic 401 redirect to login

### Request Interceptor

```javascript
// Auto-add JWT to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### Response Interceptor

```javascript
// Auto-redirect on 401 Unauthorized
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt');
      router.push('/login');
    }
    return Promise.reject(error);
  }
);
```

### Example API Calls

#### Register User

```javascript
import { registerUser } from '@/api/authApi';

const userData = {
  email: 'user@example.com',
  password: 'SecurePass123!',
  firstName: 'John',
  lastName: 'Doe'
};

try {
  const response = await registerUser(userData);
  console.log(response.data.message);
} catch (error) {
  console.error(error.response?.data?.message);
}
```

#### Login

```javascript
import { login } from '@/api/authApi';

const credentials = {
  email: 'user@example.com',
  password: 'SecurePass123!'
};

try {
  const response = await login(credentials);
  const { token } = response.data;
  localStorage.setItem('jwt', token);
} catch (error) {
  console.error(error.response?.data?.message);
}
```

---

## 🎨 Styling

### Tailwind CSS

The app uses Tailwind CSS utility classes for styling. Custom classes are defined in `src/style.css`:

- `.btn` — Base button styles
- `.btn-primary` — Primary button (blue)
- `.btn-secondary` — Secondary button (gray)
- `.input-field` — Form input styling
- `.form-label` — Form label styling
- `.card` — Card container

### Custom Classes

```css
/* src/style.css */

.btn {
  @apply px-4 py-2 rounded font-semibold transition-colors duration-200;
}

.btn-primary {
  @apply bg-blue-600 text-white hover:bg-blue-700;
}

.input-field {
  @apply w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500;
}
```

### Responsive Design

All components use Tailwind's responsive utilities:

```vue
<div class="container mx-auto px-4 sm:px-6 lg:px-8">
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
    <!-- Content -->
  </div>
</div>
```

---

## 🛣️ Routing

### Route Configuration

`src/router/index.js` defines all application routes:

```javascript
const routes = [
  { path: '/', component: HomePage },
  { path: '/login', component: LoginPage },
  { path: '/register', component: RegisterPage },
  { path: '/dashboard', component: DashboardPage, meta: { requiresAuth: true } },
  // ... more routes
];
```

### Protected Routes

Routes with `meta: { requiresAuth: true }` require authentication:

```javascript
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('jwt');
  
  if (to.meta.requiresAuth && !token) {
    next('/login');
  } else {
    next();
  }
});
```

### Navigation

```vue
<!-- Template navigation -->
<router-link to="/dashboard">Dashboard</router-link>

<!-- Programmatic navigation -->
<script>
this.$router.push('/login');
// or
import { useRouter } from 'vue-router';
const router = useRouter();
router.push('/dashboard');
</script>
```

---

## 🗄️ State Management

### Pinia Store

`src/stores/authStore.js` manages authentication state:

```javascript
import { defineStore } from 'pinia';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('jwt') || null
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.token
  },
  
  actions: {
    setToken(token) {
      this.token = token;
      localStorage.setItem('jwt', token);
    },
    
    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('jwt');
    }
  }
});
```

### Using the Store

In components:

```vue
<script setup>
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

const handleLogin = async () => {
  const response = await login(credentials);
  authStore.setToken(response.data.token);
};

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<template>
  <div v-if="authStore.isAuthenticated">
    Welcome, user!
  </div>
</template>
```

---

## ⚙️ Configuration

### Environment Variables

Create `.env.local` for local overrides:

```
VITE_API_URL=http://localhost:8080/api
```

Access in code:

```javascript
const apiUrl = import.meta.env.VITE_API_URL;
```

### Vite Config

`vite.config.js` includes dev server proxy:

```javascript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
```

---

## 🐛 Troubleshooting

### API Requests Failing (CORS)

**Symptom**: `CORS policy` errors in browser console

**Solutions**:
1. Verify backend is running at `http://localhost:8080`
2. Check backend CORS configuration allows `localhost:5173`
3. Ensure Vite proxy is configured in `vite.config.js`

### 401 Unauthorized Errors

**Symptom**: Redirected to login unexpectedly

**Solutions**:
1. Check JWT token exists: `localStorage.getItem('jwt')`
2. Verify token hasn't expired (24-hour lifetime)
3. Ensure `Authorization` header is sent: Check Network tab in DevTools
4. Re-login to get fresh token

### Translations Not Working

**Symptom**: Keys displayed instead of translations (e.g., `login.title`)

**Solutions**:
1. Verify key exists in all locale files (`en.json`, `es.json`, `zh-TW.json`)
2. Check i18n is properly initialized in `main.js`
3. Use correct syntax: `$t('key')` in templates, `t('key')` in script

### Build Errors (Terser)

**Symptom**: `Cannot find module 'terser'`

**Solution**:
```bash
npm install --save-dev terser
```

### Hot Module Replacement Not Working

**Symptom**: Changes not reflected in browser

**Solutions**:
1. Check Vite dev server is running: `npm run dev`
2. Hard refresh browser: `Ctrl+Shift+R` (Windows/Linux) or `Cmd+Shift+R` (Mac)
3. Clear browser cache
4. Restart Vite dev server

---

## 🧰 Development Tips

### Vue DevTools

Install Vue DevTools browser extension for debugging:

- **Chrome**: [Vue.js devtools](https://chrome.google.com/webstore/detail/vuejs-devtools/nhdogjmejiglipccpnnnanhbledajbpd)
- **Firefox**: [Vue.js devtools](https://addons.mozilla.org/en-US/firefox/addon/vue-js-devtools/)

### Component Patterns

This project uses a mix of **Options API** and **Composition API** (`<script setup>`):

**Options API Example**:
```vue
<script>
export default {
  data() {
    return {
      count: 0
    };
  },
  methods: {
    increment() {
      this.count++;
    }
  }
};
</script>
```

**Composition API Example**:
```vue
<script setup>
import { ref } from 'vue';

const count = ref(0);
const increment = () => count.value++;
</script>
```

### Code Organization

- **Components**: Reusable UI elements (forms, buttons, modals)
- **Views**: Full page components with routing
- **Stores**: Global state (auth, user data)
- **API**: Backend communication layer

---

## 📦 Key Dependencies

- **vue**: ^3.3.4 — Core framework
- **vue-router**: ^4.2.5 — Routing
- **pinia**: ^2.1.7 — State management
- **vue-i18n**: ^9.8.0 — Internationalization
- **axios**: ^1.6.0 — HTTP client
- **tailwindcss**: ^3.3.5 — CSS framework

### Dev Dependencies

- **vite**: ^5.0.0 — Build tool
- **@vitejs/plugin-vue**: ^4.5.0 — Vue plugin for Vite
- **autoprefixer**: ^10.4.16 — CSS vendor prefixes
- **postcss**: ^8.4.32 — CSS processing

---

## 📝 Code Conventions

### Component Naming

- **PascalCase** for component files: `LoginForm.vue`, `DashboardPage.vue`
- **kebab-case** in templates: `<login-form />`, `<dashboard-page />`

### SFC Order

Single File Component order:

1. `<template>` — HTML structure
2. `<script>` — Component logic
3. `<style scoped>` — Component styles

### Props & Events

```vue
<script setup>
// Props
const props = defineProps({
  title: String,
  isVisible: Boolean
});

// Events
const emit = defineEmits(['close', 'submit']);

const handleSubmit = () => {
  emit('submit', formData);
};
</script>
```

---

## 📄 License

MIT
<img src="https://img.shields.io/badge/frontend-Vue%203%20%2B%20Vite-orange" alt="Vue 3" />

# Frontend — Vue 3 SPA

### Build

---

## Setup

### Prerequisites
- Node.js 18+

### 1. Install Dependencies
```bash
npm install
```

### 2. Run Dev Server
```bash
npm run dev
```
Visit: [http://localhost:5173](http://localhost:5173)

---

## Build & Lint

- Build: `npm run build`
- Lint: `npm run lint`

---

## i18n

- Locales: `src/locales/en.json`, `es.json`, `zh-TW.json`
- Use `$t('key')` in templates
- All UI fields are i18n-enabled

---

## Project Structure

```
src/
	api/         # Axios API wrappers
	components/  # Vue components
	locales/     # i18n
	router/      # Vue Router
	stores/      # Pinia stores
	views/       # Pages
	style.css    # Tailwind + custom classes
```

---

## API Integration

- Axios proxy: `/api` → backend
- JWT in `localStorage`, auto-injected

---

## Troubleshooting

- **API 401**: Check backend is running, JWT present
- **i18n missing**: Add key to all locale files

---

## License

MIT
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
