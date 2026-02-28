<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">Welcome</h2>
      <p class="text-gray-600 text-center mb-8">
        User Registration System with OAuth2 Authentication
      </p>
      <div class="flex flex-col gap-4">
        <router-link to="/register" class="btn btn-primary text-center">
          Create Account
        </router-link>
        <router-link to="/login" class="btn btn-outline text-center">
          Sign In
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'HomePage',
})
</script>
