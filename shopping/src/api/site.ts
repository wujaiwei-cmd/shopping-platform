/**
 * 站点信息（店铺名 / 公告 / 客服 / 页脚政策）
 * 商家直接改 shopping/public/site.json 即可，不需要动代码。
 */
import { ref } from 'vue'

export interface SiteService {
  wechat: string
  phone: string
  email: string
  hours: string
}

export interface SiteFooter {
  about: string
  shipping: string
  refund: string
  privacy: string
  icp: string
}

export interface SiteConfig {
  storeName: string
  slogan: string
  notice: string
  service: SiteService
  footer: SiteFooter
}

export const defaultSite: SiteConfig = {
  storeName: '喜乐严选',
  slogan: '产地直供 · 品质好物',
  notice: '',
  service: { wechat: '', phone: '', email: '', hours: '' },
  footer: { about: '', shipping: '', refund: '', privacy: '', icp: '' },
}

let cached: SiteConfig | null = null

/** 全局共享的站点信息，其它页面直接用 {{ siteRef.storeName }} */
export const siteRef = ref<SiteConfig>(defaultSite)

export async function loadSite(): Promise<SiteConfig> {
  if (cached) return cached

  try {
    const base = import.meta.env.BASE_URL || '/'
    const res = await fetch(base + 'site.json?t=' + Date.now(), { cache: 'no-store' })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    cached = {
      ...defaultSite,
      ...data,
      service: { ...defaultSite.service, ...(data.service || {}) },
      footer: { ...defaultSite.footer, ...(data.footer || {}) },
    }
    siteRef.value = cached
    return cached
  } catch (error) {
    console.warn('[site] 读取站点配置失败，使用默认值：', error)
    cached = defaultSite
    siteRef.value = cached
    return cached
  }
}