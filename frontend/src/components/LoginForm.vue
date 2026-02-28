<template>
  <div class="form-group">
    <form v-if="!show2FAInput" @submit.prevent="handleLogin" class="space-y-4">
      <div>
        <label for="email" class="form-label">Email Address</label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          placeholder="user@example.com"
          class="input-field"
          required
        />
        <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
      </div>

      <div>
        <label for="password" class="form-label">Password</label>
        <div class="relative">
          <input
            id="password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="••••••••"
            class="input-field pr-10"
            required
          />
          <button
            type="button"
            @click="showPassword = !showPassword"
            class="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
          >
            {{ showPassword ? '👁️' : '👁️‍🗨️' }}
          </button>
        </div>
        <p v-if="errors.password" class="form-error">{{ errors.password }}</p>
      </div>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <button
        type="submit"
        class="btn btn-primary w-full"
        :disabled="authStore.loading"
      >
        {{ authStore.loading ? 'Signing In...' : 'Sign In' }}
      </button>
    </form>

    <!-- 2FA Verification Input -->
    <form v-else @submit.prevent="handleTwoFAVerify" class="space-y-4">
      <div class="bg-blue-50 p-3 rounded-lg mb-4">
        <p class="text-sm text-blue-800">
          Enter the 6-digit code from your authenticator app
        </p>
      </div>

      <div>
        <label for="twoFACode" class="form-label">Authentication Code</label>
        <input
          id="twoFACode"
          v-model="twoFACode"
          type="text"
          placeholder="000000"
          maxlength="6"
          class="input-field text-center text-2xl tracking-widest"
          required
        />
        <p v-if="errors.twoFACode" class="form-error">{{ errors.twoFACode }}</p>
      </div>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <div class="flex gap-2">
        <button
          type="submit"
          class="btn btn-primary flex-1"
          :disabled="authStore.loading || twoFACode.length !== 6"
        >
          {{ authStore.loading ? 'Verifying...' : 'Verify' }}
        </button>
        <button
          type="button"
          @click="useBackupCode"
          class="btn btn-outline flex-1"
        >
          Use Backup Code
        </button>
      </div>
    </form>

    <!-- Backup Code Input -->
    <form v-if="showBackupCodeInput" @submit.prevent="handleBackupCodeVerify" class="space-y-4">
      <div class="bg-blue-50 p-3 rounded-lg mb-4">
        <p class="text-sm text-blue-800">
          Enter one of your backup codes (format: XXXX-XXXX)
        </p>
      </div>

      <div>
        <label for="backupCode" class="form-label">Backup Code</label>
        <input
          id="backupCode"
          v-model="backupCode"
          type="text"
          placeholder="XXXX-XXXX"
          class="input-field"
          required
        />
        <p v-if="errors.backupCode" class="form-error">{{ errors.backupCode }}</p>
      </div>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <div class="flex gap-2">
        <button
          type="submit"
          class="btn btn-primary flex-1"
          :disabled="authStore.loading"
        >
          {{ authStore.loading ? 'Verifying...' : 'Verify Code' }}
        </button>
        <button
          type="button"
          @click="backToTwoFA"
          class="btn btn-outline flex-1"
        >
          Back to 2FA
        </button>
      </div>
    </form>

    <div v-if="!show2FAInput && !showBackupCodeInput" class="flex flex-col gap-2 mt-4">
      <router-link to="/forgot-password" class="text-sm text-blue-600 hover:underline">
        Forgot Password?
      </router-link>
      <p class="text-center text-sm text-gray-600">
        Don't have an account?
        <router-link to="/register" class="text-blue-600 hover:underline">
          Sign Up
        </router-link>
      </p>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'LoginForm',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const form = {
      email: '',
      password: '',
    }

    const errors = {
      email: '',
      password: '',
      twoFACode: '',
      backupCode: '',
    }

    const showPassword = ref(false)
    const show2FAInput = ref(false)
    const showBackupCodeInput = ref(false)
    const twoFACode = ref('')
    const backupCode = ref('')

    const validateLoginForm = () => {
      errors.email = !form.email ? 'Email is required' : ''
      errors.password = !form.password ? 'Password is required' : ''
      return !Object.values({ email: errors.email, password: errors.password }).some(e => e)
    }

    const handleLogin = async () => {
      if (!validateLoginForm()) return

      try {
        const response = await authStore.login({
          email: form.email,
          password: form.password,
        })

        // If 2FA is required, show 2FA input
        if (response && response.requiresTwoFA) {
          show2FAInput.value = true
        } else {
          // Redirect to dashboard
          setTimeout(() => {
            router.push('/dashboard')
          }, 500)
        }
      } catch (error) {
        // Error is handled by the store
      }
    }

    const validateTwoFACode = () => {
      if (!twoFACode.value) {
        errors.twoFACode = '2FA code is required'
        return false
      }
      if (!/^\d{6}$/.test(twoFACode.value)) {
        errors.twoFACode = '2FA code must be 6 digits'
        return false
      }
      errors.twoFACode = ''
      return true
    }

    const handleTwoFAVerify = async () => {
      if (!validateTwoFACode()) return

      try {
        await authStore.verify2FA({
          email: form.email,
          code: twoFACode.value,
        })

        // Redirect to dashboard
        setTimeout(() => {
          router.push('/dashboard')
        }, 500)
      } catch (error) {
        // Error is handled by the store
      }
    }

    const useBackupCode = () => {
      showBackupCodeInput.value = true
    }

    const backToTwoFA = () => {
      showBackupCodeInput.value = false
      twoFACode.value = ''
      errors.twoFACode = ''
    }

    const validateBackupCode = () => {
      if (!backupCode.value) {
        errors.backupCode = 'Backup code is required'
        return false
      }
      if (!/^[A-Z0-9]{4}-[A-Z0-9]{4}$/.test(backupCode.value)) {
        errors.backupCode = 'Backup code must be in format XXXX-XXXX'
        return false
      }
      errors.backupCode = ''
      return true
    }

    const handleBackupCodeVerify = async () => {
      if (!validateBackupCode()) return

      try {
        await authStore.verifyBackupCode({
          email: form.email,
          code: backupCode.value,
        })

        // Redirect to dashboard
        setTimeout(() => {
          router.push('/dashboard')
        }, 500)
      } catch (error) {
        // Error is handled by the store
      }
    }

    return {
      form,
      errors,
      authStore,
      showPassword,
      show2FAInput,
      showBackupCodeInput,
      twoFACode,
      backupCode,
      handleLogin,
      handleTwoFAVerify,
      useBackupCode,
      backToTwoFA,
      handleBackupCodeVerify,
    }
  },
})
</script>
