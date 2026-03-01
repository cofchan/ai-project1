<template>
  <div class="form-group">
    <div v-if="!setupInitiated" class="space-y-4">
      <div class="bg-blue-50 p-4 rounded-lg">
        <p class="text-sm text-blue-800 mb-4">
          Two-Factor Authentication adds an extra layer of security to your account. You'll need an authenticator app like Google Authenticator, Authy, or Microsoft Authenticator.
        </p>
      </div>

      <button
        @click="initiateSetup"
        class="btn btn-primary w-full"
        :disabled="loading"
      >
        {{ loading ? 'Setting Up...' : 'Start 2FA Setup' }}
      </button>

      <p v-if="error" class="form-error bg-red-50 p-3 rounded">{{ error }}</p>
    </div>

    <div v-else-if="!codeVerified" class="space-y-4">
      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
        <h3 class="font-semibold text-yellow-900 mb-2">Step 1: Scan QR Code</h3>
        <p class="text-sm text-yellow-800 mb-4">
          Open your authenticator app and scan this QR code:
        </p>
      </div>

      <div v-if="qrCodeImage" class="flex justify-center bg-white p-4 rounded-lg border">
        <img :src="qrCodeImage" :alt="'QR code for ' + email" class="w-48 h-48" />
      </div>

      <div class="bg-gray-50 p-4 rounded-lg">
        <p class="text-xs text-gray-600 mb-2">Can't scan? Enter this key manually:</p>
        <code class="text-sm bg-white p-2 rounded block break-words">{{ secret }}</code>
      </div>

      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200 mt-4">
        <h3 class="font-semibold text-yellow-900 mb-2">Step 2: Verify Code</h3>
        <p class="text-sm text-yellow-800 mb-4">
          Enter the 6-digit code from your authenticator app to verify:
        </p>
      </div>

      <div>
        <label for="verificationCode" class="form-label">Verification Code</label>
        <input
          id="verificationCode"
          v-model="verificationCode"
          type="text"
          placeholder="{{$t('codePlaceholder')}}"
          maxlength="6"
          class="input-field text-center text-2xl tracking-widest"
          required
        />
        <p v-if="codeError" class="form-error">{{ codeError }}</p>
      </div>

      <button
        @click="verifyCode"
        class="btn btn-primary w-full"
        :disabled="loading || verificationCode.length !== 6"
      >
        {{ loading ? 'Verifying...' : 'Verify & Continue' }}
      </button>

      <button
        @click="cancelSetup"
        class="btn btn-outline w-full"
        :disabled="loading"
      >
        Cancel
      </button>

      <p v-if="error" class="form-error bg-red-50 p-3 rounded">{{ error }}</p>
    </div>

    <div v-else class="space-y-4">
      <div class="bg-green-50 p-4 rounded-lg border border-green-200">
        <h3 class="font-semibold text-green-900 mb-2">✓ Verification Successful!</h3>
        <p class="text-sm text-green-800">
          Your authenticator app is now set up. Save your backup codes in a safe place.
        </p>
      </div>

      <div class="bg-red-50 p-4 rounded-lg border border-red-200">
        <h3 class="font-semibold text-red-900 mb-2">⚠️ Important: Save Backup Codes</h3>
        <p class="text-xs text-red-800 mb-3">
          These backup codes are the only way to recover your account if you lose access to your authenticator app. Each code can be used once. Store them safely.
        </p>
      </div>

      <div class="bg-gray-50 p-4 rounded-lg border">
        <div class="grid grid-cols-2 gap-2">
          <code
            v-for="(code, index) in backupCodes"
            :key="index"
            class="text-sm bg-white p-2 rounded text-center font-mono"
          >
            {{ code }}
          </code>
        </div>
      </div>

      <div class="flex gap-2">
        <button
          @click="downloadBackupCodes"
          class="btn btn-secondary flex-1"
        >
          📥 Download Codes
        </button>
        <button
          @click="copyBackupCodes"
          class="btn btn-secondary flex-1"
        >
          📋 Copy Codes
        </button>
      </div>

      <button
        @click="finishSetup"
        class="btn btn-primary w-full"
      >
        I've Saved My Codes - Continue
      </button>

      <p v-if="successMessage" class="form-success bg-green-50 p-3 rounded text-green-800">
        {{ successMessage }}
      </p>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import authApi from '@/api/authApi'

export default defineComponent({
  name: 'TwoFASetupForm',
  emits: ['setup-complete'],
  setup(_, { emit }) {
    const email = ref('')
    const setupInitiated = ref(false)
    const codeVerified = ref(false)
    const loading = ref(false)
    const error = ref('')
    const codeError = ref('')
    const successMessage = ref('')

    const qrCodeImage = ref('')
    const secret = ref('')
    const backupCodes = ref([])
    const verificationCode = ref('')

    const initiateSetup = async () => {
      loading.value = true
      error.value = ''

      try {
        const response = await authApi.setupTwoFA()
        secret.value = response.data.secret
        qrCodeImage.value = response.data.qrCodeBase64
        backupCodes.value = response.data.backupCodes
        email.value = response.data.email || 'your account'
        setupInitiated.value = true
      } catch (err) {
        error.value = err.response?.data?.message || 'Failed to initiate 2FA setup'
      } finally {
        loading.value = false
      }
    }

    const verifyCode = async () => {
      codeError.value = ''
      if (!/^\d{6}$/.test(verificationCode.value)) {
        codeError.value = 'Please enter a valid 6-digit code'
        return
      }

      loading.value = true
      error.value = ''

      try {
        await authApi.confirmTwoFA({
          secret: secret.value,
          code: verificationCode.value,
        })
        codeVerified.value = true
      } catch (err) {
        error.value = err.response?.data?.message || 'Invalid verification code. Please try again.'
      } finally {
        loading.value = false
      }
    }

    const cancelSetup = () => {
      setupInitiated.value = false
      codeVerified.value = false
      verificationCode.value = ''
      error.value = ''
      secret.value = ''
      qrCodeImage.value = ''
      backupCodes.value = []
    }

    const downloadBackupCodes = () => {
      const content = backupCodes.value.join('\n')
      const element = document.createElement('a')
      element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(content))
      element.setAttribute('download', 'backup-codes.txt')
      element.style.display = 'none'
      document.body.appendChild(element)
      element.click()
      document.body.removeChild(element)
      successMessage.value = 'Backup codes downloaded!'
    }

    const copyBackupCodes = () => {
      const content = backupCodes.value.join('\n')
      navigator.clipboard.writeText(content).then(() => {
        successMessage.value = 'Backup codes copied to clipboard!'
        setTimeout(() => {
          successMessage.value = ''
        }, 3000)
      })
    }

    const finishSetup = () => {
      emit('setup-complete')
    }

    return {
      email,
      setupInitiated,
      codeVerified,
      loading,
      error,
      codeError,
      successMessage,
      qrCodeImage,
      secret,
      backupCodes,
      verificationCode,
      initiateSetup,
      verifyCode,
      cancelSetup,
      downloadBackupCodes,
      copyBackupCodes,
      finishSetup,
    }
  },
})
</script>
