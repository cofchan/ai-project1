<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">
        Recover Password
      </h2>
      <ForgotPasswordForm />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import ForgotPasswordForm from '@/components/ForgotPasswordForm.vue'

export default defineComponent({
  name: 'ForgotPasswordPage',
  components: {
    ForgotPasswordForm,
  },
})
</script>
