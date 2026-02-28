<template>
  <div class="form-group">
    <form @submit.prevent="handleRegister" class="space-y-4">
      <div>
        <label for="fullName" class="form-label">Full Name</label>
        <input
          id="fullName"
          v-model="form.fullName"
          type="text"
          placeholder="John Doe"
          class="input-field"
          required
        />
        <p v-if="errors.fullName" class="form-error">{{ errors.fullName }}</p>
      </div>

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
        <input
          id="password"
          v-model="form.password"
          type="password"
          placeholder="••••••••"
          class="input-field"
          required
        />
        <p class="text-xs text-gray-500 mt-1">
          Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char (@$!%*?&)
        </p>
        <p v-if="errors.password" class="form-error">{{ errors.password }}</p>
      </div>

      <div>
        <label for="passwordConfirm" class="form-label">Confirm Password</label>
        <input
          id="passwordConfirm"
          v-model="form.passwordConfirm"
          type="password"
          placeholder="••••••••"
          class="input-field"
          required
        />
        <p v-if="errors.passwordConfirm" class="form-error">{{ errors.passwordConfirm }}</p>
      </div>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <button
        type="submit"
        class="btn btn-primary w-full"
        :disabled="authStore.loading"
      >
        {{ authStore.loading ? 'Creating Account...' : 'Create Account' }}
      </button>
    </form>

    <p class="text-center text-sm text-gray-600 mt-4">
      Already have an account?
      <router-link to="/login" class="text-blue-600 hover:underline">
        Sign In
      </router-link>
    </p>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'RegistrationForm',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const form = {
      email: '',
      password: '',
      passwordConfirm: '',
      fullName: '',
    }

    const errors = {
      email: '',
      password: '',
      passwordConfirm: '',
      fullName: '',
    }

    const validateForm = () => {
      errors.email = !form.email ? 'Email is required' : ''
      errors.password = !form.password ? 'Password is required' : ''
      errors.passwordConfirm = !form.passwordConfirm ? 'Password confirmation is required' : ''
      errors.fullName = !form.fullName ? 'Full name is required' : ''

      if (form.password !== form.passwordConfirm) {
        errors.passwordConfirm = 'Passwords do not match'
      }

      return !Object.values(errors).some(e => e)
    }

    const handleRegister = async () => {
      if (!validateForm()) return

      try {
        await authStore.register({
          email: form.email,
          password: form.password,
          passwordConfirm: form.passwordConfirm,
          fullName: form.fullName,
        })

        // Redirect to email verification
        setTimeout(() => {
          router.push('/verify-email')
        }, 1000)
      } catch (error) {
        // Error is handled by the store
      }
    }

    return {
      form,
      errors,
      authStore,
      handleRegister,
    }
  },
})
</script>
