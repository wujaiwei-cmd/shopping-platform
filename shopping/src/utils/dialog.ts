/**
 * 原生 confirm 的安全兜底
 * ---------------------------------------------------------------
 * Chrome 在同一个页面弹过几次 alert/confirm 后，会提示
 * 「阻止此页面创建更多对话框」。一旦用户勾选，之后：
 *   - alert(...) 直接什么都不显示
 *   - confirm(...) 瞬间返回 false
 * 于是 `if (!confirm(...)) return;` 这类代码就变成“点了完全没反应”。
 *
 * 这里用耗时判断：人类不可能在 5ms 内点完确认框，
 * 所以“很快返回 false”基本可以断定是被浏览器拦截了，此时按“继续执行”处理，
 * 避免用户点了按钮却毫无反应。
 */

let dialogsBlocked = false

export function confirmSafe(message: string): boolean {
  if (dialogsBlocked) return true

  const started = performance.now()
  const answer = window.confirm(message)
  const elapsed = performance.now() - started

  if (!answer && elapsed < 5) {
    dialogsBlocked = true
    return true
  }

  return answer
}

/** 供调试：当前是否判定为“弹窗被拦截” */
export const areDialogsBlocked = (): boolean => dialogsBlocked
