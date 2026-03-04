<template>
  <div class="form-group">
    <!-- Login Form -->
    <form v-if="!show2FAInput && !showBackupCodeInput" @submit.prevent="handleLogin" class="space-y-4">
      <div class="space-y-4 mb-4">
        <button
          type="button"
          class="btn btn-primary w-full flex items-center justify-center gap-2"
          @click="redirectToOAuth('google')"
        >
          <span><img src="https://www.svgrepo.com/show/355037/google.svg" alt="Google" width="20" /></span>
          {{ $t('signInWithGoogle') }}
        </button>
        <button
          type="button"
          class="btn btn-outline w-full flex items-center justify-center gap-2"
          @click="redirectToOAuth('github')"
        >
          <span><img src="https://www.svgrepo.com/show/349419/github.svg" alt="GitHub" width="20" /></span>
          {{ $t('signInWithGithub') }}
        </button>
      </div>

      <div class="relative my-4">
        <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-gray-300"></div></div>
        <div class="relative flex justify-center text-sm"><span class="px-2 bg-white text-gray-500">{{ $t('orContinueWith') }}</span></div>
      </div>

      <div>
        <label for="email" class="form-label">{{ $t('emailAddress') }}</label>
        <input
          id="email"
          v-model="form.email"
          @input="clearFieldError('email')"
          type="email"
          class="input-field"
          required
        />
        <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
      </div>

      <div>
        <label for="password" class="form-label">{{ $t('password') }}</label>
        <input
          id="password"
          v-model="form.password"
          @input="clearFieldError('password')"
          type="password"
          class="input-field"
          required
        />
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
        {{ authStore.loading ? $t('loggingIn') : $t('login') }}
      </button>
    </form>

    <!-- 2FA Verification Input -->
    <form v-else-if="show2FAInput && !showBackupCodeInput" @submit.prevent="handleTwoFAVerify" class="space-y-4">
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
          {{ authStore.loading ? $t('verifying') : $t('verify') }}
        </button>
        <button
          type="button"
          @click="backToTwoFA"
          class="btn btn-outline flex-1"
        >
          {{ $t('backTo2FA') }}
        </button>
      </div>
    </form>

    <div v-if="!show2FAInput && !showBackupCodeInput" class="flex flex-col gap-2 mt-4">
      <router-link to="/forgot-password" class="text-sm text-blue-600 hover:underline">
        {{ $t('forgotPassword') }}
      </router-link>
      <p class="text-center text-sm text-gray-600">
        {{ $t('dontHaveAccount') }}
        <router-link to="/register" class="text-blue-600 hover:underline">
          {{ $t('register') }}
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
    const show2FAInput = ref(false)
    const showBackupCodeInput = ref(false)
    const twoFACode = ref('')
    const backupCode = ref('')
    // input refs for managing focus
    const twoFACodeRef = ref(null)
    const backupCodeRef = ref(null)

    const form = ref({ email: '', password: '' })
    const errors = ref({ email: '', password: '', twoFACode: '', backupCode: '' })

    // remember credentials so we can resend code
    const storedCredentials = ref({ email: '', password: '' })

    const validateLoginForm = () => {
      errors.value.email = !form.value.email ? 'Email is required' : ''
      errors.value.password = !form.value.password ? 'Password is required' : ''
      return !Object.values({ email: errors.value.email, password: errors.value.password }).some(e => e)
    }

    const handleLogin = async () => {
      if (!validateLoginForm()) return

      try {
        const response = await authStore.login({
          email: form.value.email,
          password: form.value.password,
        })

        // If 2FA is required, show 2FA input
        if (response && response.requiresTwoFA) {
          // keep credentials around so we can resend later
          storedCredentials.value = { ...form.value }
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
        errors.value.twoFACode = '2FA code is required'
        return false
      }
      if (!/^\d{6}$/.test(twoFACode.value)) {
        errors.value.twoFACode = '2FA code must be 6 digits'
        return false
      }
      errors.value.twoFACode = ''
      return true
    }

    const handleTwoFAVerify = async () => {
      if (!validateTwoFACode()) return

      try {
        await authStore.verify2FA({
          email: form.value.email,
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
        errors.value.backupCode = 'Backup code is required'
        return
      }
      // simple pattern check XXXX-XXXX
      if (!/^[A-Z0-9]{4}-[A-Z0-9]{4}$/i.test(backupCode.value)) {
        errors.value.backupCode = 'Format should be XXXX-XXXX'
        return
      }
      errors.value.backupCode = ''
      try {
        await authStore.verifyBackupCode({
          email: form.value.email,
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
      errors.value.twoFACode = ''
      // focus backup input once shown
      nextTick(() => backupCodeRef.value?.focus())
    }

    const backToTwoFA = () => {
      showBackupCodeInput.value = false
      show2FAInput.value = true
      backupCode.value = ''
      errors.value.backupCode = ''
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
      errors.value[field] = ''
      authStore.error = null
    }

    // OAuth2 login redirect
    const redirectToOAuth = (provider) => {
      window.location.href = `/api/oauth2/authorization/${provider}`
    }

    return {
      authStore,
      form,
      errors,
      show2FAInput,
      showBackupCodeInput,
      twoFACode,
      backupCode,
      twoFACodeRef,
      backupCodeRef,
      redirectToOAuth,
      handleLogin,
      handleTwoFAVerify,
      useBackupCode,
      backToTwoFA,
      handleBackupCodeVerify,
      resendCode,
      clearFieldError,
    }
  },
})
</script>
