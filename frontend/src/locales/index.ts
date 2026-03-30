import { createI18n } from 'vue-i18n'
import zh from './zh'
import en from './en'

// 从 localStorage 获取语言配置，默认为中文
const currentLang = localStorage.getItem('language') || 'zh'

const i18n = createI18n({
  legacy: false, // 使用 Composition API 必须设置为 false
  locale: currentLang, // 默认语言
  fallbackLocale: 'en', // 回退语言
  messages: {
    zh,
    en
  }
})

export default i18n
