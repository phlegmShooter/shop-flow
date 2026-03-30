/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

import 'vue'
import { DefineDateTimeFormat, DefineLocaleMessage, DefineNumberFormat } from 'vue-i18n'

declare module 'vue' {
  export interface ComponentCustomProperties {
    $t: (key: string, ...args: any[]) => string
    $tm: (key: string) => any[] | { [p: string]: any }
    $d: (value: number | Date, key?: string | DefineDateTimeFormat, locale?: string) => string
    $n: (value: number, key?: string | DefineNumberFormat, locale?: string) => string
  }
}
