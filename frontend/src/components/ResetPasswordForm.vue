<template>
  <div class="form-group">
    <form @submit.prevent="handleResetPassword" class="space-y-4">
      <div>
        <label for="token" class="form-label">Reset Token</label>
        <input
          id="token"
          v-model="form.token"
          type="text"
          placeholder="Token from reset link"
          class="input-field"
          required
        />
        <p v-if="errors.token" class="form-error">{{ errors.token }}</p>
      </div>

      <div>
        <label for="newPassword" class="form-label">New Password</label>
        <div class="relative">
          <input
            id="newPassword"
            v-model="form.newPassword"
            :type="showNewPassword ? 'text' : 'password'"
            placeholder="••••••••"
            class="input-field pr-10"
            required
          />
          <button
            type="button"
            @click="showNewPassword = !showNewPassword"
            class="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
          >
            {{ showNewPassword ? '👁️' : '👁️‍🗨️' }}
          </button>
        </div>
        <p class="text-xs text-gray-500 mt-1">
          Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char (@$!%*?&)
        </p>
        <p v-if="errors.newPassword" class="form-error">{{ errors.newPassword }}</p>
      </div>

      <div>
        <label for="confirmPassword" class="form-label">Confirm Password</label>
        <div class="relative">
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            placeholder="••••••••"
            class="input-field pr-10"
            required
          />
          <button
            type="button"
            @click="showConfirmPassword = !showConfirmPassword"
            class="absolute right-3 top-3 text-gray-500 hover:text-gray-700"
          >
            {{ showConfirmPassword ? '👁️' : '👁️‍🗨️' }}
          </button>
        </div>
        <p v-if="errors.confirmPassword" class="form-error">{{ errors.confirmPassword }}</p>
      </div>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <p v-if="successMessage" class="form-success bg-green-50 p-3 rounded text-green-800">
        {{ successMessage }}
      </p>

      <button
        type="submit"
        class="btn btn-primary w-full"
        :disabled="authStore.loading"
      >
        {{ authStore.loading ? 'Resetting...' : 'Reset Password' }}
      </button>
    </form>

    <router-link to="/login" class="block text-center text-sm text-blue-600 hover:underline mt-4">
      Back to Login
    </router-link>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'ResetPasswordForm',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const form = {
      token: '',
      newPassword: '',
      confirmPassword: '',
    }

    const errors = {
      token: '',
      newPassword: '',
      confirmPassword: '',
    }

    const successMessage = ref('')
    const showNewPassword = ref(false)
    const showConfirmPassword = ref(false)

    const validateForm = () => {
      errors.token = !form.token ? 'Reset token is required' : ''
      errors.newPassword = !form.newPassword ? 'New password is required' : ''
      errors.confirmPassword = !form.confirmPassword ? 'Password confirmation is required' : ''

      if (form.newPassword !== form.confirmPassword) {
        errors.confirmPassword = 'Passwords do not match'
      }

      return !Object.values(errors).some(e => e)
    }

    const handleResetPassword = async () => {
      if (!validateForm()) return

      try {
        successMessage.value = ''
        await authStore.resetPassword({
          token: form.token,
          newPassword: form.newPassword,
        })

        successMessage.value =
          'Password reset successfully! Redirecting to login page...'

        setTimeout(() => {
          router.push('/login')
        }, 2000)
      } catch (error) {
        // Error is handled by the store
      }
    }

    return {
      form,
      errors,
      successMessage,
      showNewPassword,
      showConfirmPassword,
      authStore,
      handleResetPassword,
    }
  },
})
</script>
