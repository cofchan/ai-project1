<template>
  <div id="app">
    <nav class="bg-white shadow">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center">
            <router-link to="/" class="flex items-center">
              <h1 class="text-2xl font-bold text-blue-600">Auth System</h1>
            </router-link>
          </div>
          <div class="flex items-center gap-4">
            <router-link
              v-if="!isAuthenticated"
              to="/login"
              class="text-gray-700 hover:text-blue-600"
            >
              Login
            </router-link>
            <router-link
              v-if="!isAuthenticated"
              to="/register"
              class="btn btn-primary"
            >
              Register
            </router-link>
            <div v-if="isAuthenticated" class="flex items-center gap-3">
              <span class="text-gray-700">{{ userName }}</span>
              <button @click="logout" class="btn btn-secondary">
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-view />
    </main>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useAuthStore } from './stores/authStore'
import { mapState, mapActions } from 'pinia'

export default defineComponent({
  name: 'App',
  computed: {
    ...mapState(useAuthStore, ['isAuthenticated', 'user']),
    userName() {
      return this.user?.fullName || this.user?.email || ''
    },
  },
  methods: {
    ...mapActions(useAuthStore, ['logout']),
  },
})
</script>

<style scoped>
</style>
