/**
 * 线上商品目录（仓库里的 shopping/public/products.json）
 * ---------------------------------------------------------------
 * 为什么不用第三方云存储：Pantry 的注册走 Google reCAPTCHA，国内网络加载不出来，
 * 所以改成“商品目录直接放在本仓库里”——访客读同域静态文件（最快、无 CORS），
 * 商家发布时由浏览器把 JSON 提交回仓库（可选一步配置，见 README）。
 *
 * 读取：GET {BASE}products.json（同域，带时间戳防缓存）
 * 发布：
 *   - 已配置 GitHub Token：直接调 Contents API 提交文件，Pages 自动重建（约 1 分钟）
 *   - 未配置：返回 JSON 让商家复制到 GitHub 网页编辑器里提交
 */

const TOKEN_KEY = 'catalog_github_token'

export interface ProductDoc {
  version: number
  updatedAt: string
  items: any[]
}

/* ------------------------------------------------------------------ */
/* 仓库信息（从当前页面推断，本地开发时回退到默认值）                   */
/* ------------------------------------------------------------------ */

export function detectRepo(): { owner: string; repo: string; branch: string; path: string } {
  const host = location.hostname
  const path = 'shopping/public/products.json'
  if (host.endsWith('.github.io')) {
    const owner = host.slice(0, -'.github.io'.length)
    const seg = location.pathname.split('/').filter(Boolean)[0]
    const repo = seg || `${owner}.github.io`
    return { owner, repo, branch: 'main', path }
  }
  return { owner: 'wujaiwei-cmd', repo: 'shopping-platform', branch: 'main', path }
}

export function githubEditUrl(): string {
  const { owner, repo, branch, path } = detectRepo()
  return `https://github.com/${owner}/${repo}/edit/${branch}/${path}`
}

export function githubFileUrl(): string {
  const { owner, repo, branch, path } = detectRepo()
  return `https://github.com/${owner}/${repo}/blob/${branch}/${path}`
}

/* ------------------------------------------------------------------ */
/* 读取已发布的商品                                                    */
/* ------------------------------------------------------------------ */

export interface PublishedDoc {
  items: any[]
  updatedAt: string
}

/** 读取线上商品文档（商品 + 更新时间） */
export async function loadPublishedDoc(): Promise<PublishedDoc | null> {
  try {
    const base = import.meta.env.BASE_URL || '/'
    const res = await fetch(`${base}products.json?t=${Date.now()}`, { cache: 'no-store' })
    if (!res.ok) return null
    const data = await res.json()
    if (Array.isArray(data)) return { items: data, updatedAt: '' }
    return {
      items: Array.isArray(data?.items) ? data.items : [],
      updatedAt: typeof data?.updatedAt === 'string' ? data.updatedAt : '',
    }
  } catch (error) {
    console.warn('[catalog] 读取线上商品目录失败：', error)
    return null
  }
}

export async function loadPublishedGoods(): Promise<any[] | null> {
  const doc = await loadPublishedDoc()
  return doc ? doc.items : null
}

/* ------------------------------------------------------------------ */
/* 发布                                                               */
/* ------------------------------------------------------------------ */

export const getToken = (): string => {
  try {
    return localStorage.getItem(TOKEN_KEY) || ''
  } catch {
    return ''
  }
}

export const setToken = (token: string): void => {
  try {
    if (token.trim()) localStorage.setItem(TOKEN_KEY, token.trim())
    else localStorage.removeItem(TOKEN_KEY)
  } catch {
    /* ignore */
  }
}

const utf8ToBase64 = (text: string): string => {
  const bytes = new TextEncoder().encode(text)
  let binary = ''
  bytes.forEach((b) => {
    binary += String.fromCharCode(b)
  })
  return btoa(binary)
}

export function buildDoc(items: any[]): ProductDoc {
  return {
    version: 1,
    updatedAt: new Date().toISOString(),
    items: items.map((item) => ({
      id: item.id,
      name: item.name,
      price: item.price,
      stock: item.stock,
      category: item.category,
      description: item.description || '',
      image: item.image || '',
      status: item.status || 'active',
      banner: !!item.banner,
      bannerImage: item.bannerImage || '',
      createdAt: item.createdAt || new Date().toISOString(),
    })),
  }
}

/** 一键发布（需要 GitHub Token）；失败时返回可读的错误信息 */
export async function publishToRepo(items: any[]): Promise<{ ok: boolean; message: string }> {
  const token = getToken()
  if (!token) return { ok: false, message: '还没有配置 GitHub Token，请用“复制 JSON”的方式发布' }

  const { owner, repo, branch, path } = detectRepo()
  const api = `https://api.github.com/repos/${owner}/${repo}/contents/${path}`
  const headers: Record<string, string> = {
    Accept: 'application/vnd.github+json',
    Authorization: `Bearer ${token}`,
    'X-GitHub-Api-Version': '2022-11-28',
  }

  try {
    // 先取当前文件的 sha（文件不存在时也可以创建，此时不需要 sha）
    let sha: string | undefined
    const current = await fetch(`${api}?ref=${branch}`, { headers, cache: 'no-store' })
    if (current.ok) {
      const info = await current.json()
      sha = info.sha
    } else if (current.status !== 404) {
      return { ok: false, message: `读取仓库文件失败（HTTP ${current.status}），Token 可能没有 contents 写权限` }
    }

    const doc = buildDoc(items)
    const body: Record<string, any> = {
      message: `chore(catalog): 发布商品目录（${doc.items.length} 件）`,
      content: utf8ToBase64(JSON.stringify(doc, null, 2) + '\n'),
      branch,
    }
    if (sha) body.sha = sha

    const res = await fetch(api, {
      method: 'PUT',
      headers: { ...headers, 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })

    if (res.ok) {
      return { ok: true, message: '已提交到仓库，GitHub Pages 大约 1 分钟后重新部署完成' }
    }
    const detail = await res.json().catch(() => ({}))
    return { ok: false, message: `发布失败（HTTP ${res.status}）：${detail.message || '未知错误'}` }
  } catch (error: any) {
    return { ok: false, message: `发布失败：${error?.message || error}` }
  }
}
