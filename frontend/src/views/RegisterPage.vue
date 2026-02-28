<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">Create Account</h2>
      <RegistrationForm />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import RegistrationForm from '@/components/RegistrationForm.vue'

export default defineComponent({
  name: 'RegisterPage',
  components: {
    RegistrationForm,
  },
})
</script>
