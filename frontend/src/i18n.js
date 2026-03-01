import { createI18n } from 'vue-i18n'
import en from './locales/en.json'
import es from './locales/es.json'
import zhTW from './locales/zh-TW.json'

const messages = { en, es, 'zh-TW': zhTW }

const i18n = createI18n({
  legacy: false,
  locale: 'en',
  fallbackLocale: 'en',
  messages,
})

export default i18n
