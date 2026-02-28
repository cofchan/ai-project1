<template>
  <div class="max-w-4xl mx-auto">
    <div class="card">
      <h2 class="text-3xl font-bold mb-6 text-gray-800">Dashboard</h2>

      <!-- User Profile Section -->
      <div v-if="user" class="space-y-6">
        <div class="bg-blue-50 p-4 rounded-lg border border-blue-200">
          <h3 class="font-semibold text-blue-900 mb-4">Profile Information</h3>
          <div class="space-y-4">
            <div class="bg-white p-3 rounded-lg">
              <p class="text-sm text-gray-600">Email</p>
              <p class="text-lg font-semibold text-gray-800">{{ user.email }}</p>
            </div>
            <div class="bg-white p-3 rounded-lg">
              <p class="text-sm text-gray-600">Full Name</p>
              <p class="text-lg font-semibold text-gray-800">
                {{ user.fullName || 'Not set' }}
              </p>
            </div>
            <div class="bg-white p-3 rounded-lg">
              <p class="text-sm text-gray-600">Email Verified</p>
              <p class="text-lg font-semibold">
                <span v-if="user.isEmailVerified" class="text-green-600">✓ Verified</span>
                <span v-else class="text-yellow-600">⚠ Not verified</span>
              </p>
            </div>
          </div>
        </div>

        <!-- Security Section -->
        <div class="bg-purple-50 p-4 rounded-lg border border-purple-200">
          <h3 class="font-semibold text-purple-900 mb-4">Security Settings</h3>
          <div v-if="!show2FASetup" class="space-y-3">
            <div class="bg-white p-3 rounded-lg flex justify-between items-center">
              <div>
                <p class="font-semibold text-gray-800">Two-Factor Authentication (2FA)</p>
                <p class="text-sm text-gray-600 mt-1">
                  {{
                    user.isTwoFAEnabled
                      ? 'Enabled - Your account is protected'
                      : 'Disabled - Add an extra layer of security'
                  }}
                </p>
              </div>
              <button
                v-if="!user.isTwoFAEnabled"
                @click="show2FASetup = true"
                class="btn btn-primary"
              >
                Enable 2FA
              </button>
              <button
                v-else
                @click="prepareDisable2FA"
                class="btn btn-outline text-red-600 border-red-300 hover:bg-red-50"
              >
                Disable 2FA
              </button>
            </div>
          </div>
          <TwoFASetupForm
            v-if="show2FASetup && !user.isTwoFAEnabled"
            @setup-complete="handle2FASetupComplete"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { mapState } from 'pinia'
import TwoFASetupForm from '@/components/TwoFASetupForm.vue'

export default defineComponent({
  name: 'DashboardPage',
  components: {
    TwoFASetupForm,
  },
  data() {
    return {
      show2FASetup: false,
    }
  },
  computed: {
    ...mapState(useAuthStore, ['user']),
  },
  methods: {
    prepareDisable2FA() {
      // This would trigger a disable 2FA flow - to be implemented
      alert('2FA disable functionality to be implemented')
    },
    handle2FASetupComplete() {
      this.show2FASetup = false
      // Refresh user data to reflect the 2FA status
      const authStore = useAuthStore()
      if (this.user) {
        this.user.isTwoFAEnabled = true
      }
    },
  },
})
</script>
