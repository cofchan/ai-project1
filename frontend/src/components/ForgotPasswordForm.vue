<template>
  <div class="form-group">
    <form @submit.prevent="handleForgotPassword" class="space-y-4">
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
        {{ authStore.loading ? 'Sending...' : 'Send Reset Link' }}
      </button>
    </form>

    <p class="text-center text-sm text-gray-600 mt-4">
      Remember your password?
      <router-link to="/login" class="text-blue-600 hover:underline">
        Back to Login
      </router-link>
    </p>

    <p class="text-center text-sm text-gray-600 mt-2">
      Don't have an account?
      <router-link to="/register" class="text-blue-600 hover:underline">
        Sign Up
      </router-link>
    </p>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export default defineComponent({
  name: 'ForgotPasswordForm',
  setup() {
    const authStore = useAuthStore()

    const form = {
      email: '',
    }

    const errors = {
      email: '',
    }

    const successMessage = ref('')

    const validateForm = () => {
      errors.email = !form.email
        ? 'Email is required'
        : !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)
          ? 'Please enter a valid email'
          : ''

      return !errors.email
    }

    const handleForgotPassword = async () => {
      if (!validateForm()) return

      try {
        successMessage.value = ''
        // pass raw string to avoid nested object payload
        await authStore.forgotPassword(form.email)

        successMessage.value =
          'Password reset link sent to your email. Please check your inbox and follow the instructions.'
      } catch (error) {
        // Error is handled by the store
      }
    }

    return {
      form,
      errors,
      successMessage,
      authStore,
      handleForgotPassword,
    }
  },
})
</script>
