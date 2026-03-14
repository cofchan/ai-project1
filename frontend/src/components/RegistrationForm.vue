<template>
  <div class="form-group">
    <form @submit.prevent="handleRegister" class="space-y-4">
      <div>
        <label for="fullName" class="form-label">{{$t('fullName') || 'Full Name'}}</label>
        <input
          id="fullName"
          v-model="form.fullName"
          type="text"
          placeholder="{{$t('fullNamePlaceholder')}}"
          class="input-field"
          required
        />
        <p v-if="errors.fullName" class="form-error">{{ errors.fullName }}</p>
      </div>

      <div>
        <label for="email" class="form-label">{{$t('emailAddress')}}</label>
        <input
          id="email"
          v-model="form.email"
          type="email"
          placeholder="{{$t('emailPlaceholder')}}"
          class="input-field"
          required
        />
        <p v-if="errors.email" class="form-error">{{ errors.email }}</p>
      </div>

      <div>
        <label for="password" class="form-label">{{$t('password')}}</label>
        <input
          id="password"
          v-model="form.password"
          type="password"
          placeholder="••••••••"
          class="input-field"
          required
        />
        <p class="text-xs text-gray-500 mt-1">
          {{$t('passwordRules') || 'Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char (@$!%*?&)'}}
        </p>
        <p v-if="errors.password" class="form-error">{{ errors.password }}</p>
      </div>

      <div>
        <label for="passwordConfirm" class="form-label">{{$t('confirmPassword') || 'Confirm Password'}}</label>
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

      <CaptchaWidget
        ref="registerCaptchaRef"
        @change="onCaptchaChange"
      />
      <p v-if="errors.captcha" class="form-error">{{ errors.captcha }}</p>

      <p v-if="authStore.error" class="form-error bg-red-50 p-3 rounded">
        {{ authStore.error }}
      </p>

      <button
        type="submit"
        class="btn btn-primary w-full"
        :disabled="authStore.loading"
      >
        {{ authStore.loading ? $t('creatingAccount') || 'Creating Account...' : $t('createAccount') || 'Create Account' }}
      </button>
    </form>

    <p class="text-center text-sm text-gray-600 mt-4">
      {{$t('alreadyHaveAccount') || 'Already have an account?'}}
      <router-link to="/login" class="text-blue-600 hover:underline">
        {{$t('signIn') || 'Sign In'}}
      </router-link>
    </p>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'
import RecaptchaWidget from './RecaptchaWidget.vue'
import CaptchaWidget from './CaptchaWidget.vue'

export default defineComponent({
  name: 'RegistrationForm',
  components: { RecaptchaWidget, CaptchaWidget },
  setup() {
    const authStore = useAuthStore()
    const router = useRouter()
    const registerCaptchaRef = ref(null)
    const captchaData = ref({ captchaToken: '', captchaAnswer: '' })

    const form = ref({
      email: '',
      password: '',
      passwordConfirm: '',
      fullName: '',
    })

    const errors = ref({
      email: '',
      password: '',
      passwordConfirm: '',
      fullName: '',
      captcha: '',
    })

    const onCaptchaChange = (data) => {
      captchaData.value = data
      errors.value.captcha = ''
    }

    const validateForm = () => {
      errors.value.email = !form.value.email ? 'Email is required' : ''
      errors.value.password = !form.value.password ? 'Password is required' : ''
      errors.value.passwordConfirm = !form.value.passwordConfirm ? 'Password confirmation is required' : ''
      errors.value.fullName = !form.value.fullName ? 'Full name is required' : ''

      if (form.value.password !== form.value.passwordConfirm) {
        errors.value.passwordConfirm = 'Passwords do not match'
      }

      if (!captchaData.value.captchaToken || !captchaData.value.captchaAnswer) {
        errors.value.captcha = 'Please complete the CAPTCHA'
      }

      return !Object.values(errors.value).some(e => e)
    }

    const handleRegister = async () => {
      if (!validateForm()) return

      try {
        await authStore.register({
          email: form.value.email,
          password: form.value.password,
          passwordConfirm: form.value.passwordConfirm,
          fullName: form.value.fullName,
          captchaToken: captchaData.value.captchaToken,
          captchaAnswer: captchaData.value.captchaAnswer,
        })

        // Redirect to email verification
        setTimeout(() => {
          router.push('/verify-email')
        }, 1000)
      } catch (error) {
        // Error is handled by the store
        registerCaptchaRef.value?.refresh()
      }
    }

    return {
      form,
      errors,
      authStore,
      handleRegister,
      registerCaptchaRef,
      onCaptchaChange,
    }
  },
})
</script>
