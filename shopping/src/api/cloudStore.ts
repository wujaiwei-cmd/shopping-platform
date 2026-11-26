/**
 * 商品表云端读写（可选功能）
 * ---------------------------------------------------------------
 * 地址来源（优先级从高到低）：
 *   1. 构建期 VITE_CLOUD_ENDPOINT / cloudConfig.PANTRY_ID
 *   2. 运行时读取 public/cloud.json 里的 pantryId（不用重新构建，改文件提交即可）
 *
 * 未配置、网络失败、服务不可用时都会安静地退回本地数据，保证站点不白屏。
 */
import { CLOUD_ENDPOINT as BUILD_ENDPOINT, pantryUrl } from './cloudConfig'

let endpoint = BUILD_ENDPOINT
let configLoaded = false
let configLoading: Promise<void> | null = null

const TIMEOUT_MS = 8000

const withTimeout = async (input: string, init?: RequestInit): Promise<Response> => {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), TIMEOUT_MS)
  try {
    return await fetch(input, { ...init, signal: controller.signal })
  } finally {
    clearTimeout(timer)
  }
}

/** 读取 public/cloud.json（只读一次）；也用于管理端显示同步状态 */
export function ensureCloudConfig(): Promise<void> {
  if (configLoaded) return Promise.resolve()
  if (configLoading) return configLoading

  configLoading = (async () => {
    try {
      if (!endpoint) {
        const base = import.meta.env.BASE_URL || '/'
        const res = await withTimeout(`${base}cloud.json?t=${Date.now()}`, { cache: 'no-store' })
        if (res.ok) {
          const config = await res.json()
          const pantryId = String(config?.pantryId || '').trim()
          if (pantryId) endpoint = pantryUrl(pantryId)
        }
      }
    } catch {
      /* 没有配置文件就当未开启 */
    } finally {
      configLoaded = true
    }
  })()

  return configLoading
}

export const isCloudEnabled = (): boolean => endpoint.length > 0

/** 读取云端商品；返回 null 表示“云端不可用，请用本地数据” */
export async function loadGoodsFromCloud(): Promise<any[] | null> {
  await ensureCloudConfig()
  if (!isCloudEnabled()) return null
  try {
    const res = await withTimeout(endpoint, { headers: { Accept: 'application/json' }, cache: 'no-store' })
    // Pantry：basket 还不存在时返回 400/404，视为空列表
    if (res.status === 400 || res.status === 404) return []
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    const data = await res.json()
    if (Array.isArray(data)) return data
    if (data && Array.isArray(data.goods)) return data.goods
    return []
  } catch (error) {
    console.warn('[cloud] 读取云端商品失败，改用本地数据：', error)
    return null
  }
}

/** 覆盖写入云端商品列表；返回是否成功 */
export async function saveGoodsToCloud(list: any[]): Promise<boolean> {
  await ensureCloudConfig()
  if (!isCloudEnabled()) return false
  try {
    const res = await withTimeout(endpoint, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(list),
    })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return true
  } catch (error) {
    console.warn('[cloud] 保存到云端失败：', error)
    return false
  }
}
