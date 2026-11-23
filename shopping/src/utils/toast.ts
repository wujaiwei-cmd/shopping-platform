/**
 * 极简 toast（替代 Element Plus 的 ElMessage）
 * 只为一个购物车页面引入整套 Element Plus（≈1MB JS + 455KB CSS）不划算，
 * 这里用 30 行原生 DOM 实现同样的轻提示。
 */

type ToastKind = 'success' | 'error' | 'warning' | 'info'

const ACCENT: Record<ToastKind, string> = {
  success: '#67c23a',
  error: '#f56c6c',
  warning: '#e6a23c',
  info: '#909399',
}

let container: HTMLDivElement | null = null

function ensureContainer(): HTMLDivElement {
  if (container && document.body.contains(container)) return container

  container = document.createElement('div')
  container.setAttribute('data-toast-container', '')
  const style = container.style
  style.position = 'fixed'
  style.top = '16px'
  style.left = '50%'
  style.transform = 'translateX(-50%)'
  style.zIndex = '99999'
  style.display = 'flex'
  style.flexDirection = 'column'
  style.alignItems = 'center'
  style.gap = '8px'
  style.pointerEvents = 'none'
  document.body.appendChild(container)
  return container
}

export function showToast(message: string, kind: ToastKind = 'info', duration = 2200): void {
  if (typeof document === 'undefined' || !message) return

  const box = document.createElement('div')
  box.textContent = message
  const style = box.style
  style.background = '#fff'
  style.color = '#303133'
  style.borderLeft = `4px solid ${ACCENT[kind]}`
  style.borderRadius = '6px'
  style.padding = '10px 16px'
  style.fontSize = '14px'
  style.lineHeight = '1.5'
  style.maxWidth = '80vw'
  style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.15)'
  style.opacity = '0'
  style.transform = 'translateY(-8px)'
  style.transition = 'opacity 0.2s ease, transform 0.2s ease'

  ensureContainer().appendChild(box)

  requestAnimationFrame(() => {
    style.opacity = '1'
    style.transform = 'translateY(0)'
  })

  window.setTimeout(() => {
    style.opacity = '0'
    style.transform = 'translateY(-8px)'
    window.setTimeout(() => box.remove(), 220)
  }, duration)
}

export const toast = {
  success: (message: string) => showToast(message, 'success'),
  error: (message: string) => showToast(message, 'error', 2800),
  warning: (message: string) => showToast(message, 'warning'),
  info: (message: string) => showToast(message, 'info'),
}
