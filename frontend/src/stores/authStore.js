import { defineStore } from 'pinia'
import authApi from '../api/authApi'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token'),
    loading: false,
    error: null,
  }),

  getters: {
    isLoggedIn: (state) => state.isAuthenticated,
    currentUser: (state) => state.user,
    authToken: (state) => state.token,
  },

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
        this.token = response.data.token
        this.user = response.data.user
        this.isAuthenticated = true
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify(response.data.user))
        
        // Check if 2FA is required
        if (response.data.requiresTwoFA) {
          return { requiresTwoFA: true }
        }
        return response.data
      } catch (err) {
        this.error = err.response?.data?.error || 'Login failed'
        throw err
      } finally {
        this.loading = false
      }
    },

    async verifyEmail(token) {
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
