<template>
  <div class="container-center">
    <div class="card max-w-md w-full">
      <h2 class="text-3xl font-bold mb-6 text-center text-gray-800">
        Verify Email
      </h2>
      <VerifyEmailForm />
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import VerifyEmailForm from '@/components/VerifyEmailForm.vue'

export default defineComponent({
  name: 'VerifyEmailPage',
  components: {
    VerifyEmailForm,
  },
})
</script>
