import { defineStore } from 'pinia'
import authApi from '../api/authApi'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token'),
    loading: false,
    error: null,
    twoFAMessage: '',
  }),

  actions: {
    async register(registrationData) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.register(registrationData)
        this.user = response.data.user
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Registration failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async login(credentials) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.login(credentials)

        // Check if 2FA is required before mutating auth state
        if (response.data.requiresTwoFA) {
          // preserve message (e.g. code sent info)
          this.twoFAMessage = response.data.message || ''
          return { requiresTwoFA: true }
        }

        // normal login with token
        this.token = response.data.token
        this.user = response.data.user
        this.isAuthenticated = true
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Login failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async verifyEmail(token) {
      // token should be a simple string; callers previously passed an object which
      // led to nested JSON payloads ({ token: { token: '...' } }) and deserialization
      // errors on the backend.  Ensure we only send the raw value and trim whitespace.
      if (typeof token === 'string') {
        token = token.trim()
      }
      this.loading = true
      this.error = null
      try {
        const response = await authApi.verifyEmail({ token })
        if (this.user) {
          this.user.isEmailVerified = true
        }
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Email verification failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async forgotPassword(email) {
      // email should be a string; callers were previously passing
      // an object which resulted in payloads like { email: { email: '...' }}
      this.loading = true
      this.error = null
      try {
        const response = await authApi.forgotPassword({ email })
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Request failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async resetPassword(resetData) {
      // trim token in case user copied with spaces
      if (resetData && typeof resetData.token === 'string') {
        resetData.token = resetData.token.trim()
      }
      this.loading = true
      this.error = null
      try {
        const response = await authApi.resetPassword(resetData)
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Password reset failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async verify2FA(twoFAData) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.verify2FA(twoFAData)
        this.token = response.data.token
        this.user = response.data.user
        this.isAuthenticated = true
        this.twoFAMessage = ''
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || '2FA verification failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async verifyBackupCode(backupCodeData) {
      this.loading = true
      this.error = null
      try {
        const response = await authApi.verifyBackupCode(backupCodeData)
        this.token = response.data.token
        this.user = response.data.user
        this.isAuthenticated = true
        this.twoFAMessage = ''
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Backup code verification failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      this.error = null
      this.twoFAMessage = ''
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },

    clearError() {
      this.error = null
    },

    setUser(user) {
      this.user = user
    },
  },
})
