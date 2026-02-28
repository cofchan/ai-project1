<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">Sign In</h2>
      <LoginForm />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import LoginForm from '@/components/LoginForm.vue'

export default defineComponent({
  name: 'LoginPage',
  components: {
    LoginForm,
  },
})
</script>
