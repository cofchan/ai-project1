<template>
  <div class="border border-gray-200 rounded-lg p-4 bg-gray-50 space-y-3">
    <div v-if="loading" class="text-sm text-gray-500">Loading CAPTCHA...</div>

    <div v-else-if="fetchError" class="text-sm text-red-500 flex items-center gap-2">
      {{ fetchError }}
      <button type="button" class="text-blue-600 underline" @click="refresh">Retry</button>
    </div>

    <template v-else>
      <div class="flex items-center gap-3">
        <canvas
          ref="canvasRef"
          width="160"
          height="56"
          class="rounded select-none"
          style="display:block"
        />
        <div class="flex flex-col gap-2">
          <button
            type="button"
            title="New challenge"
            class="text-gray-400 hover:text-gray-600 text-xl leading-none"
            @click="refresh"
          >&#8635;</button>
          <button
            type="button"
            title="Listen to CAPTCHA"
            class="text-gray-400 hover:text-gray-600 text-xl leading-none"
            :disabled="!question || speakingAudio"
            @click="speakCaptcha"
            aria-label="Listen to CAPTCHA code"
          >🔊</button>
        </div>
      </div>
      <input
        v-model="answer"
        type="text"
        maxlength="4"
        placeholder="Enter the 4 characters above"
        class="input-field uppercase"
        autocomplete="off"
        spellcheck="false"
        @input="emitChange"
      />
    </template>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, watch, nextTick } from 'vue'
import authApi from '@/api/authApi'

const W = 160
const H = 56

function rnd(min, max) {
  return Math.random() * (max - min) + min
}

function drawCaptcha(canvas, code) {
  console.log('drawCaptcha called:', { code, canvas: !!canvas })
  if (!canvas || !code) {
    console.warn('drawCaptcha: missing canvas or code')
    return
  }
  
  try {
    const ctx = canvas.getContext('2d')
    if (!ctx) {
      console.error('Failed to get 2D context')
      return
    }

    // Clear canvas
    ctx.clearRect(0, 0, W, H)
    
    // White background
    ctx.fillStyle = 'white'
    ctx.fillRect(0, 0, W, H)

    // Black border so we know canvas is rendered
    ctx.strokeStyle = 'black'
    ctx.lineWidth = 2
    ctx.strokeRect(2, 2, W - 4, H - 4)

    // Draw the 4 characters in large black text
    ctx.font = 'bold 40px monospace'
    ctx.textAlign = 'center'
    ctx.textBaseline = 'middle'
    ctx.fillStyle = 'black'
    
    // Draw each character with spacing
    const charW = W / code.length
    for (let i = 0; i < code.length; i++) {
      const x = charW * i + charW / 2
      const y = H / 2
      ctx.fillText(code[i], x, y)
    }
    
    console.log('drawCaptcha completed successfully')
  } catch (err) {
    console.error('drawCaptcha error:', err)
  }
}



export default defineComponent({
  name: 'CaptchaWidget',
  emits: ['change'],
  setup(_, { emit, expose }) {
    const loading = ref(false)
    const fetchError = ref('')
    const question = ref('')
    const token = ref('')
    const answer = ref('')
    const canvasRef = ref(null)
    const speakingAudio = ref(false)

    const renderCanvas = () => {
      console.log('renderCanvas called:', { canvasRef: canvasRef.value, question: question.value })
      if (canvasRef.value && question.value) {
        console.log('Drawing CAPTCHA with question:', question.value)
        drawCaptcha(canvasRef.value, question.value)
      } else {
        console.log('Cannot render: canvas or question missing')
      }
    }

    const speakCaptcha = () => {
      if (!question.value || !('speechSynthesis' in window)) {
        console.warn('Speech synthesis not available or no question')
        return
      }

      // Cancel any ongoing speech
      speechSynthesis.cancel()
      speakingAudio.value = true

      // Create utterance with individual characters spoken clearly
      const text = question.value.split('').join(', ')
      const utterance = new SpeechSynthesisUtterance(text)
      utterance.rate = 0.8 // Slow down for clarity
      utterance.pitch = 1
      utterance.volume = 1

      utterance.onend = () => {
        speakingAudio.value = false
      }

      utterance.onerror = (event) => {
        console.error('Speech synthesis error:', event.error)
        speakingAudio.value = false
      }

      speechSynthesis.speak(utterance)
    }

    const emitChange = () => {
      emit('change', { captchaToken: token.value, captchaAnswer: answer.value })
    }

    const refresh = async () => {
      loading.value = true
      fetchError.value = ''
      answer.value = ''
      token.value = ''
      question.value = ''
      emit('change', { captchaToken: '', captchaAnswer: '' })
      
      // Add a timeout to prevent hanging
      const timeoutPromise = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('CAPTCHA request timeout')), 10000)
      )
      
      try {
        console.log('Fetching CAPTCHA...')
        const res = await Promise.race([authApi.getCaptcha(), timeoutPromise])
        console.log('CAPTCHA API Response:', res.data)
        
        // Verify response has required fields
        if (!res.data || !res.data.captchaToken || !res.data.captchaQuestion) {
          console.error('Invalid CAPTCHA response structure:', res.data)
          fetchError.value = 'Invalid CAPTCHA response from server'
          loading.value = false
          return
        }
        
        question.value = res.data.captchaQuestion
        token.value = res.data.captchaToken
        console.log('Question set to:', question.value)
        
        // Set loading to false FIRST so canvas renders
        loading.value = false
        
        // Wait for canvas to appear in DOM
        await nextTick()
        console.log('About to render canvas with:', { question: question.value, canvasRef: !!canvasRef.value })
        renderCanvas()
      } catch (e) {
        console.error('CAPTCHA fetch error:', e)
        fetchError.value = `Failed to load CAPTCHA: ${e.message || 'Unknown error'}`
        loading.value = false
      }
    }

    const getData = () => ({ captchaToken: token.value, captchaAnswer: answer.value })

    onMounted(refresh)
    expose({ refresh, getData })

    return { loading, fetchError, question, answer, canvasRef, refresh, emitChange, speakCaptcha, speakingAudio }
  },
})
</script>
