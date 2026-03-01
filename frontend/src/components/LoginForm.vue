<template>
  <div class="form-group">
    <form v-if="!show2FAInput" @submit.prevent="handleLogin" class="space-y-4">
      <div>
        <label for="email" class="form-label">{{$t('emailAddress')}}</label>
        <input
          id="email"
          v-model="form.email"
          @input="clearFieldError('email')"
          type="email"
          placeholder="{{$t('emailPlaceholder')}}"
          class="input-field"
          required
        />
        <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
      </div>

      <div>
        <label for="password" class="form-label">{{$t('password')}}</label>
        <div class="relative">
          <input
            id="password"
            v-model="form.password"
            @input="clearFieldError('password')"
            :type="showPassword ? 'text' : 'password'"
            placeholder="{{$t('passwordPlaceholder')}}"
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
        {{ authStore.loading ? $t('signingIn') : $t('signIn') }}
      </button>
    </form>

    <!-- 2FA Verification Input -->
    <form v-else @submit.prevent="handleTwoFAVerify" class="space-y-4">
      <div class="bg-blue-50 p-3 rounded-lg mb-4">
        <p class="text-sm text-blue-800">
          {{ authStore.twoFAMessage || $t('enterAuthCode') }}
        </p>
        <p class="text-xs text-blue-600 mt-1">
          <button
            type="button"
            class="underline hover:text-blue-800"
            @click="resendCode"
            :disabled="authStore.loading"
          >
            {{$t('resendCode')}}
          </button>
          <span v-if="authStore.loading">(sending...)</span>
        </p>
      </div>

      <div>
        <label for="twoFACode" class="form-label">{{$t('verificationCode')}}</label>
        <input
          id="twoFACode"
          ref="twoFACodeRef"
          v-model="twoFACode"
          @input="clearFieldError('twoFACode')"
          type="text"
          placeholder="{{$t('codePlaceholder')}}"
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
          {{ authStore.loading ? $t('verifying') : $t('verify') }}
        </button>
        <button
          type="button"
          @click="useBackupCode"
          class="btn btn-outline flex-1"
        >
          {{$t('useBackupCode')}}
        </button>
      </div>

      <div class="mt-4 text-center">
        <button
          type="button"
          class="text-sm text-gray-600 hover:underline"
          @click="() => { show2FAInput=false; showBackupCodeInput=false; form.password=''; }"
        >
          {{$t('backToLogin')}}
        </button>
      </div>
    </form>

    <!-- Backup Code Input -->
    <form v-if="showBackupCodeInput" @submit.prevent="handleBackupCodeVerify" class="space-y-4">
      <div class="text-center mb-2">
        <button
          type="button"
          class="text-sm text-gray-600 hover:underline"
          @click="backToTwoFA"
        >
          {{$t('backTo2FA')}}
        </button>
      </div>
      <div class="bg-blue-50 p-3 rounded-lg mb-4">
        <p class="text-sm text-blue-800">
          {{$t('backupCodeInstructions')}}
        </p>
      </div>

      <div>
        <label for="backupCode" class="form-label">{{$t('backupCode')}}</label>
        <input
          id="backupCode"
          v-model="backupCode"
          type="text"
          placeholder="{{$t('backupCodeFormat')}}"
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
import { defineComponent, ref, nextTick, watch } from 'vue'
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

    // input refs for managing focus
    const twoFACodeRef = ref(null)
    const backupCodeRef = ref(null)

    // remember credentials so we can resend code
    const storedCredentials = ref({ email: '', password: '' })

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
          // keep credentials around so we can resend later
          storedCredentials.value = { ...form }
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
        errors.twoFACode = $t('errorTwoFACodeDigits') || '2FA code must be 6 digits'
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

    const handleBackupCodeVerify = async () => {
      if (!backupCode.value) {
        errors.backupCode = $t('errorBackupCodeRequired') || 'Backup code is required'
        return
      }
      // simple pattern check XXXX-XXXX
      if (!/^[A-Z0-9]{4}-[A-Z0-9]{4}$/i.test(backupCode.value)) {
        errors.backupCode = $t('errorBackupCodeFormat') || 'Format should be XXXX-XXXX'
        return
      }
      errors.backupCode = ''
      try {
        await authStore.verifyBackupCode({
          email: form.email,
          backupCode: backupCode.value,
        })
        setTimeout(() => {
          router.push('/dashboard')
        }, 500)
      } catch (error) {
        // handled by store
      }
    }

    const useBackupCode = () => {
      showBackupCodeInput.value = true
      show2FAInput.value = false
      // clear twoFA code and errors
      twoFACode.value = ''
      errors.twoFACode = ''
      // focus backup input once shown
      nextTick(() => backupCodeRef.value?.focus())
    }

    const backToTwoFA = () => {
      showBackupCodeInput.value = false
      show2FAInput.value = true
      backupCode.value = ''
      errors.backupCode = ''
      nextTick(() => twoFACodeRef.value?.focus())
    }

    const resendCode = async () => {
      if (storedCredentials.value.email && storedCredentials.value.password) {
        authStore.error = null
        try {
          const resp = await authStore.login(storedCredentials.value)
          // message updated automatically by store
          show2FAInput.value = true
        } catch (e) {
          // keep existing error display
        }
      }
    }

    watch(show2FAInput, visible => {
      if (visible) {
        nextTick(() => twoFACodeRef.value?.focus())
      }
    })

    watch(showBackupCodeInput, visible => {
      if (visible) {
        nextTick(() => backupCodeRef.value?.focus())
      }
    })

    // clear error when user edits any field
    const clearFieldError = field => {
      errors[field] = ''
      authStore.error = null
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
