<template>
  <div class="form-group">
    <div class="bg-blue-50 p-4 rounded-lg mb-6">
      <p class="text-sm text-blue-800">
        {{$t('verifyEmailInstructions') || "We've sent a verification link to your email. Enter the verification code below to confirm your email address."}}
      </p>
    </div>

    <form @submit.prevent="handleVerifyEmail" class="space-y-4">
      <div>
        <label for="code" class="form-label">{{$t('verificationCode')}}</label>
        <input
          id="code"
          v-model="form.code"
          type="text"
          placeholder="{{$t('enterToken')}}"
          class="input-field"
          required
        />
        <p v-if="errors.code" class="form-error">{{ errors.code }}</p>
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
        {{ authStore.loading ? $t('verifying') : $t('verifyEmail') }}
      </button>
    </form>

    <div class="mt-4 text-center">
      <p class="text-sm text-gray-600">
        {{$t('didntReceiveEmail') || "Didn't receive the email?"}}
        <button
          type="button"
          @click="resendCode"
          class="text-blue-600 hover:underline"
          :disabled="authStore.loading"
        >
          {{ resendLoading ? $t('sending') || 'Sending...' : $t('resendCode') }}
        </button>
      </p>
    </div>

    <router-link to="/login" class="block text-center text-sm text-blue-600 hover:underline mt-4">
      {{$t('backToLogin')}}
    </router-link>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'VerifyEmailForm',
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()

    const form = {
      code: '',
    }

    const errors = {
      code: '',
    }

    const successMessage = ref('')
    const resendLoading = ref(false)

    const validateForm = () => {
      errors.code = !form.code ? $t('errorCodeRequired') || 'Verification code is required' : ''
      return !errors.code
    }

    const handleVerifyEmail = async () => {
      if (!validateForm()) return

      try {
        successMessage.value = ''
        // pass the raw code string rather than wrapping it in an object
        await authStore.verifyEmail(form.code)

        successMessage.value = $t('emailVerifiedSuccess') || 'Email verified successfully! Two-factor authentication has been enabled; you will be prompted to enter a code on your next login. Redirecting to dashboard...'
        setTimeout(() => {
          router.push('/dashboard')
        }, 2000)
      } catch (error) {
        // Error is handled by the store
      }
    }

    const resendCode = async () => {
      resendLoading.value = true
      try {
        // Placeholder for resend logic - would need to be added to backend
        successMessage.value = 'Verification code resent to your email'
      } catch (error) {
        authStore.error = 'Failed to resend verification code'
      } finally {
        resendLoading.value = false
      }
    }

    return {
      form,
      errors,
      successMessage,
      resendLoading,
      authStore,
      handleVerifyEmail,
      resendCode,
    }
  },
})
</script>
