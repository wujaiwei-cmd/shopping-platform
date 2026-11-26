/**
 * 云端商品同步配置
 * ---------------------------------------------------------------
 * 商品默认只存在各个浏览器的 localStorage 里（纯本地演示）。
 * 想让「商家上架一次，所有访客都能看到」，把商品表放到一个免费的
 * JSON 存储服务（Pantry）上：
 *
 *  1. 打开 https://getpantry.cloud/ ，点 “Create a Pantry”，填邮箱 + 名字
 *  2. 复制生成的 Pantry ID（一串 UUID）
 *  3. 把它填到 shopping/public/cloud.json 里：
 *        { "pantryId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx" }
 *     直接在 GitHub 网页上改这个文件并提交即可，Pages 会自动重新部署。
 *
 * 也可以在构建时用 VITE_CLOUD_ENDPOINT 指定任意兼容 endpoint（本地联调 / 换服务商）。
 *
 * 说明：Pantry ID 不是密钥而是一个“桶”的地址，前端代码里可见；
 * 它只暴露商品列表，不涉及用户、订单等隐私数据，被清空也能重新上架恢复。
 */

export const PANTRY_ID = ''

const BASKET = 'goods'

// 构建期覆盖（本地联调 / 换服务商时用）
const ENDPOINT_OVERRIDE = ((import.meta.env as any).VITE_CLOUD_ENDPOINT || '').trim()

export const pantryUrl = (pantryId: string): string =>
  `https://getpantry.cloud/apiv1/pantry/${pantryId.trim()}/basket/${BASKET}`

/** 构建期就确定的地址；为空时会在运行时读取 public/cloud.json */
export const CLOUD_ENDPOINT = ENDPOINT_OVERRIDE || (PANTRY_ID.trim() ? pantryUrl(PANTRY_ID) : '')
