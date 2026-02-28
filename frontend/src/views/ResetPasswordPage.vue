<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">
        Reset Password
      </h2>
      <ResetPasswordForm />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import ResetPasswordForm from '@/components/ResetPasswordForm.vue'

export default defineComponent({
  name: 'ResetPasswordPage',
  components: {
    ResetPasswordForm,
  },
})
</script>
