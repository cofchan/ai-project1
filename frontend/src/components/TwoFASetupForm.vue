<template>
  <div class="form-group">
    <div v-if="!setupInitiated" class="space-y-4">
      <div class="bg-blue-50 p-4 rounded-lg">
        <p class="text-sm text-blue-800 mb-4">
          {{ $t('twoFADescription1') }}
        </p>
      </div>

      <button
        @click="initiateSetup"
        class="btn btn-primary w-full"
        :disabled="loading"
      >
        {{ loading ? $t('settingUp') : $t('startTwoFASetup') }}
      </button>

      <p v-if="error" class="form-error bg-red-50 p-3 rounded">{{ error }}</p>
    </div>

    <div v-else-if="!codeVerified" class="space-y-4">
      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
        <h3 class="font-semibold text-yellow-900 mb-2">{{ $t('step1ScanQRCode') }}</h3>
        <p class="text-sm text-yellow-800 mb-4">
          {{ $t('openAuthenticatorApp') }}
        </p>
      </div>

      <div v-if="qrCodeImage" class="flex justify-center bg-white p-4 rounded-lg border">
        <img :src="qrCodeImage" :alt="'QR code for ' + email" class="w-48 h-48" />
      </div>

      <div class="bg-gray-50 p-4 rounded-lg">
        <p class="text-xs text-gray-600 mb-2">{{ $t('cantScanEnterKeyManually') }}</p>
        <code class="text-sm bg-white p-2 rounded block break-words">{{ secret }}</code>
      </div>

      <div class="bg-yellow-50 p-4 rounded-lg border border-yellow-200 mt-4">
        <h3 class="font-semibold text-yellow-900 mb-2">{{ $t('step2VerifyCode') }}</h3>
        <p class="text-sm text-yellow-800 mb-4">
          {{ $t('enterSixDigitCode') }}
        </p>
      </div>

      <div>
        <label for="verificationCode" class="form-label">{{ $t('verificationCode') }}</label>
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
        {{ loading ? $t('verifying') : $t('verifyAndContinue') }}
      </button>

      <button
        @click="cancelSetup"
        class="btn btn-outline w-full"
        :disabled="loading"
      >
        {{ $t('cancel') }}
      </button>

      <p v-if="error" class="form-error bg-red-50 p-3 rounded">{{ error }}</p>
    </div>

    <div v-else class="space-y-4">
      <div class="bg-green-50 p-4 rounded-lg border border-green-200">
        <h3 class="font-semibold text-green-900 mb-2">{{ $t('verificationSuccessful') }}</h3>
        <p class="text-sm text-green-800">
          {{ $t('authenticatorAppSetup') }}
        </p>
      </div>

      <div class="bg-red-50 p-4 rounded-lg border border-red-200">
        <h3 class="font-semibold text-red-900 mb-2">{{ $t('saveBackupCodesWarning') }}</h3>
        <p class="text-xs text-red-800 mb-3">
          {{ $t('backupCodesWarningText') }}
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
          {{ $t('downloadCodes') }}
        </button>
        <button
          @click="copyBackupCodes"
          class="btn btn-secondary flex-1"
        >
          {{ $t('copyCodes') }}
        </button>
      </div>

      <button
        @click="finishSetup"
        class="btn btn-primary w-full"
      >
        {{ $t('savedCodesContinue') }}
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
        error.value = err.response?.data?.message || this.$t('failedInitiate2FA')
      } finally {
        loading.value = false
      }
    }

    const verifyCode = async () => {
      codeError.value = ''
      if (!/^\d{6}$/.test(verificationCode.value)) {
        codeError.value = this.$t('invalidSixDigitCode')
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
        error.value = err.response?.data?.message || this.$t('invalidVerificationCode')
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
    }

    const copyBackupCodes = () => {
      const content = backupCodes.value.join('\n')
      navigator.clipboard.writeText(content).then(() => {
        successMessage.value = ''
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
