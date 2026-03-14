import axios from 'axios'

const API_BASE_URL = '/api/auth'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Add token to requests
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Handle response errors
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default {
  register(data) {
    return apiClient.post('/register', data)
  },

  login(data) {
    return apiClient.post('/login', data)
  },

  getCaptcha() {
    return apiClient.get('/captcha')
  },

  verifyEmail(data) {
    return apiClient.post('/verify-email', data)
  },

  forgotPassword(data) {
    return apiClient.post('/forgot-password', data)
  },

  resetPassword(data) {
    return apiClient.post('/reset-password', data)
  },

  getProfile() {
    return apiClient.get('/profile')
  },

  logout() {
    return apiClient.post('/logout')
  },

  setupTwoFA() {
    return apiClient.post('/2fa/setup')
  },

  confirmTwoFA(data) {
    return apiClient.post('/2fa/confirm', data)
  },

  verify2FA(data) {
    return apiClient.post('/2fa/verify', data)
  },

  disableTwoFA(data) {
    return apiClient.post('/2fa/disable', data)
  },

  verifyBackupCode(data) {
    return apiClient.post('/2fa/backup-code', data)
  },

  regenerateBackupCodes() {
    return apiClient.post('/2fa/backup-codes/regenerate')
  },
}
