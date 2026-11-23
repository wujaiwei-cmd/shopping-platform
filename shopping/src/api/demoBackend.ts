/**
 * 演示模式后端（Demo Backend）
 * ---------------------------------------------------------------
 * 这个项目的接口原本全部指向 `http://localhost:8081`（本机的 Spring Boot）。
 * 但 GitHub Pages 只能托管静态文件，跑不了 Java + MySQL，所以线上打开时
 * 所有请求都会 connection refused —— 表现就是「页面能看，但什么都点不动」。
 *
 * 这里在浏览器里模拟了一整套后端：
 *   - 拦截发往 localhost:8081 / 同源 /api/* 的 fetch 请求
 *   - 用 localStorage 当数据库（每个浏览器各自一份数据，互不干扰）
 *   - 接口路径、请求参数、返回字段全部对齐 backend/ 里的 Spring Controller
 *
 * 因此线上（以及任何没启动后端的环境）打开，登录、商品、购物车、下单、
 * 退款、收藏、地址、评价、反馈、管理端都能真实跑通一遍。
 *
 * 真正启动本机后端调试时，可用 `?demo=off` 或 localStorage.demoMode = 'off'
 * 关掉它，请求就会照常发到 http://localhost:8081。
 */

const DB_KEY = 'shopping_demo_db_v2'
const TOKEN_PREFIX = 'demo-token'
const BASE = import.meta.env.BASE_URL || '/'

const API_HOSTS = [
  'localhost:8081',
  '127.0.0.1:8081',
  'localhost:8080',
  '127.0.0.1:8080',
]

/* ------------------------------------------------------------------ */
/* 类型                                                                */
/* ------------------------------------------------------------------ */

type AnyRecord = Record<string, any>

interface DemoDb {
  base: string
  seq: Record<string, number>
  users: AnyRecord[]
  goods: AnyRecord[]
  carts: AnyRecord[]
  orders: AnyRecord[]
  orderItems: AnyRecord[]
  favorites: AnyRecord[]
  addresses: AnyRecord[]
  reviews: AnyRecord[]
  feedbacks: AnyRecord[]
}

interface Ctx {
  method: string
  path: string
  segs: string[]
  query: URLSearchParams
  headers: Record<string, string>
  body: AnyRecord
}

interface ApiResult {
  code: number
  message: string
  data?: any
}

/* ------------------------------------------------------------------ */
/* 工具函数                                                            */
/* ------------------------------------------------------------------ */

const imageUrl = (file: string) => `${BASE.replace(/\/+$/, '')}/images/${file}`

/** 与 Jackson 输出接近的本地时间字符串：2026-11-23T15:04:05 */
function localIso(date: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())}T${p(date.getHours())}:${p(date.getMinutes())}:${p(date.getSeconds())}`
}

const HOUR = 3600 * 1000
const DAY = 24 * HOUR
const ago = (ms: number) => localIso(new Date(Date.now() - ms))

function nextId(db: DemoDb, key: string): number {
  db.seq[key] = (db.seq[key] || 0) + 1
  return db.seq[key]
}

const toNumber = (value: any, fallback = 0): number => {
  const n = Number(value)
  return Number.isFinite(n) ? n : fallback
}

/* ------------------------------------------------------------------ */
/* 种子数据                                                            */
/* ------------------------------------------------------------------ */

function seedDb(): DemoDb {
  const goodsSeed = [
    { name: '千年老山参', price: 1299, stock: 25, category: '健康', image: 'qnlss.webp', description: '长白山野生人参，足年足龄，整支礼盒装，适合滋补送礼。' },
    { name: '野生蜂王浆', price: 399, stock: 60, category: '健康', image: 'ysfwj.jpg', description: '高山百花蜜源，新鲜冷冻蜂王浆，每日一勺，元气满满。' },
    { name: '人形何首乌', price: 899, stock: 12, category: '健康', image: 'rxhsw.jpg', description: '仿野生种植何首乌，块根饱满，配黄酒蒸制更佳。' },
    { name: '极品燕窝', price: 1580, stock: 18, category: '食品', image: 'jpyw.jpg', description: '印尼雨季头期盏，盏形完整、少毛少杂，炖煮后口感顺滑。' },
    { name: '九天贡胶', price: 699, stock: 35, category: '健康', image: 'jtgj.jpg', description: '古法九提九炙，胶质浓稠，切块即食，女性滋补常备。' },
    { name: '长白山鲜参礼盒', price: 899, stock: 20, category: '食品', image: 'qnlss.webp', description: '鲜参带土直发，附赠礼盒与食用说明，节日送礼体面。' },
    { name: '高原蜂巢蜜', price: 199, stock: 88, category: '食品', image: 'ysfwj.jpg', description: '整块蜂巢，含天然蜂蜡，直接嚼食，甜而不腻。' },
    { name: '何首乌洗发皂', price: 79, stock: 150, category: '美妆', image: 'rxhsw.jpg', description: '冷制手工皂，添加何首乌提取物，温和清洁，养护发根。' },
    { name: '参茸养生礼盒', price: 2399, stock: 8, category: '健康', image: 'qnlss.webp', description: '人参 + 鹿茸 + 枸杞组合装，商务送礼首选。' },
    { name: '阿胶固元膏', price: 259, stock: 66, category: '食品', image: 'jtgj.jpg', description: '阿胶配伍核桃黑芝麻，切片即食，办公室下午茶。' },
    { name: '燕窝保湿面膜', price: 129, stock: 120, category: '美妆', image: 'jpyw.jpg', description: '燕窝提取物 + 玻尿酸，补水保湿，贴合不闷痘。' },
    { name: '养生炖盅', price: 159, stock: 45, category: '家居', image: 'jtgj.jpg', description: '陶瓷隔水炖盅，一人食容量，炖燕窝炖银耳都合适。' },
  ]

  const db: DemoDb = {
    base: BASE,
    seq: { user: 2, goods: goodsSeed.length, cart: 0, order: 0, orderItem: 0, favorite: 2, address: 1, review: 2, feedback: 2 },
    users: [
      {
        id: 1,
        username: 'admin',
        password: 'admin123',
        email: 'admin@example.com',
        phone: '13800138000',
        enabled: true,
        isAdmin: true,
        role: 'admin',
        createdAt: ago(120 * DAY),
      },
      {
        id: 2,
        username: 'demo',
        password: 'demo123',
        email: 'demo@example.com',
        phone: '13900139000',
        enabled: true,
        isAdmin: false,
        role: 'user',
        createdAt: ago(30 * DAY),
      },
    ],
    goods: goodsSeed.map((item, index) => ({
      id: index + 1,
      name: item.name,
      price: item.price,
      stock: item.stock,
      category: item.category,
      description: item.description,
      image: imageUrl(item.image),
      status: 'active',
      sales: 12 + index * 7,
      createdAt: ago((20 - index) * DAY),
      updatedAt: null,
    })),
    carts: [],
    orders: [],
    orderItems: [],
    favorites: [
      { id: 1, userId: 2, goodsId: 1, createdAt: ago(6 * DAY) },
      { id: 2, userId: 2, goodsId: 5, createdAt: ago(3 * DAY) },
    ],
    addresses: [
      {
        id: 1,
        userId: 2,
        name: '张三',
        phone: '13900139000',
        province: '浙江省',
        city: '杭州市',
        district: '西湖区',
        address: '文三路 100 号 3 幢 501 室',
        isDefault: true,
        createdAt: ago(20 * DAY),
        updatedAt: null,
      },
    ],
    reviews: [
      {
        id: 1,
        reviewId: 1,
        productId: 1,
        customerId: 2,
        customerName: 'de**',
        starCount: 5,
        rating: 5,
        reviewText: '参形完整，泡酒很合适，家里老人很满意。',
        content: '参形完整，泡酒很合适，家里老人很满意。',
        submittedAt: ago(5 * DAY),
        createdAt: ago(5 * DAY),
      },
      {
        id: 2,
        reviewId: 2,
        productId: 1,
        customerId: 2,
        customerName: '小**',
        starCount: 4,
        rating: 4,
        reviewText: '包装很用心，就是快递稍微慢了一点。',
        content: '包装很用心，就是快递稍微慢了一点。',
        submittedAt: ago(2 * DAY),
        createdAt: ago(2 * DAY),
      },
    ],
    feedbacks: [
      {
        id: 1,
        userId: 2,
        username: 'demo',
        type: 'feedback',
        title: '希望增加更多支付方式',
        content: '现在只有微信和支付宝，能不能加上银行卡支付？',
        contact: 'demo@example.com',
        isProcessed: false,
        processedNote: null,
        createdAt: ago(5 * HOUR),
        processedAt: null,
      },
      {
        id: 2,
        userId: null,
        username: 'guest',
        type: 'complaint',
        title: '物流更新不及时',
        content: '下单两天了物流信息还没更新，希望能优化。',
        contact: '138****0000',
        isProcessed: true,
        processedNote: '已联系快递核实，并向用户致歉。',
        createdAt: ago(3 * DAY),
        processedAt: ago(2 * DAY),
      },
    ],
  }

  // 预置几笔订单，让「我的订单」和管理端「数据概览」打开就有内容
  const seedOrder = (opts: {
    status: string
    paymentMethod: string
    goodsIndex: number
    quantity: number
    createdAt: string
    refundStatus?: string | null
  }) => {
    const goods = db.goods[opts.goodsIndex]
    const orderId = nextId(db, 'order')
    const totalAmount = Number((goods.price * opts.quantity).toFixed(2))
    const order = {
      id: orderId,
      userId: 2,
      orderNo: `ORD${opts.createdAt.replace(/\D/g, '').slice(0, 14)}${String(1000 + orderId)}`,
      totalAmount,
      status: opts.status,
      shippingName: '张三',
      shippingPhone: '13900139000',
      shippingAddress: '浙江省 杭州市 西湖区 文三路 100 号 3 幢 501 室',
      paymentMethod: opts.paymentMethod,
      createdAt: opts.createdAt,
      updatedAt: opts.createdAt,
      refundStatus: opts.refundStatus ?? null,
      refundReason: null,
      refundRequestedAt: null,
      refundProcessedAt: null,
      refundNote: null,
    }
    db.orders.push(order)

    const itemId = nextId(db, 'orderItem')
    db.orderItems.push({
      id: itemId,
      orderId,
      goodsId: goods.id,
      goodsName: goods.name,
      price: goods.price,
      quantity: opts.quantity,
      image: goods.image,
      description: goods.description,
      categoryName: goods.category,
      createdAt: opts.createdAt,
    })
  }

  seedOrder({ status: 'paid', paymentMethod: 'wechat', goodsIndex: 1, quantity: 1, createdAt: ago(2 * HOUR) })
  seedOrder({ status: 'shipped', paymentMethod: 'alipay', goodsIndex: 0, quantity: 1, createdAt: ago(2 * DAY) })
  seedOrder({ status: 'completed', paymentMethod: 'wechat', goodsIndex: 3, quantity: 2, createdAt: ago(6 * DAY) })

  return db
}

/* ------------------------------------------------------------------ */
/* 存储                                                                */
/* ------------------------------------------------------------------ */

let memoryDb: DemoDb | null = null

function normalizeImagePaths(db: DemoDb): void {
  // 早期版本存的是 qnlss.png，后来换成了体积小得多的 webp，这里做个兼容
  const legacy: Record<string, string> = { 'qnlss.png': 'qnlss.webp' }
  const fix = (row: AnyRecord) => {
    const image = String(row.image || '')
    const matched = /\/images\/([^/]+)$/.exec(image)
    if (!matched) return
    const file = legacy[matched[1]] || matched[1]
    row.image = imageUrl(file)
  }
  db.goods.forEach(fix)
  db.orderItems.forEach(fix)
}

function loadDb(): DemoDb {
  if (memoryDb) return memoryDb

  let parsed: DemoDb | null = null
  try {
    const raw = localStorage.getItem(DB_KEY)
    if (raw) parsed = JSON.parse(raw) as DemoDb
  } catch {
    parsed = null
  }

  if (!parsed || !Array.isArray(parsed.goods) || parsed.goods.length === 0) {
    parsed = seedDb()
  }

  parsed.base = BASE
  normalizeImagePaths(parsed)
  memoryDb = parsed
  saveDb()
  return parsed
}

function saveDb(): void {
  if (!memoryDb) return
  try {
    localStorage.setItem(DB_KEY, JSON.stringify(memoryDb))
  } catch {
    /* 隐私模式 / 配额不足时只保留内存副本 */
  }
}

/** 重置演示数据（浏览器控制台执行 __demoReset() 即可恢复初始状态） */
export function resetDemoData(): void {
  memoryDb = null
  try {
    localStorage.removeItem(DB_KEY)
  } catch {
    /* ignore */
  }
  loadDb()
}

/* ------------------------------------------------------------------ */
/* 鉴权                                                                */
/* ------------------------------------------------------------------ */

function tokenOf(user: AnyRecord): string {
  return `${TOKEN_PREFIX}.${user.id}.${Math.random().toString(36).slice(2, 10)}`
}

function currentUser(ctx: Ctx, db: DemoDb): AnyRecord | null {
  let token = ctx.headers['x-auth-token'] || ctx.headers['x-auth-token'.toLowerCase()] || ''
  if (!token) {
    const auth = ctx.headers['authorization'] || ''
    if (/^Bearer\s+/i.test(auth)) token = auth.replace(/^Bearer\s+/i, '')
  }
  token = String(token || '').trim()
  if (!token.startsWith(`${TOKEN_PREFIX}.`)) return null
  const userId = Number(token.split('.')[1])
  if (!Number.isFinite(userId)) return null
  return db.users.find((u) => u.id === userId) || null
}

function publicUser(user: AnyRecord): AnyRecord {
  return {
    id: user.id,
    username: user.username,
    email: user.email,
    phone: user.phone,
    enabled: user.enabled,
    isAdmin: !!user.isAdmin,
    role: user.role || (user.isAdmin ? 'admin' : 'user'),
    createdAt: user.createdAt,
  }
}

/* ------------------------------------------------------------------ */
/* 响应                                                                */
/* ------------------------------------------------------------------ */

const ok = (data?: any, message = 'success'): ApiResult => ({ code: 200, message, data })
const fail = (message: string, code = 400): ApiResult => ({ code, message })

function makeResponse(result: ApiResult): Response {
  const status = result.code === 200 ? 200 : result.code === 401 ? 401 : result.code === 403 ? 403 : 400
  return new Response(
    JSON.stringify({ code: result.code, message: result.message, data: result.data, timestamp: Date.now() }),
    { status, headers: { 'Content-Type': 'application/json; charset=utf-8' } },
  )
}

/* ------------------------------------------------------------------ */
/* 业务处理                                                            */
/* ------------------------------------------------------------------ */

function requireUser(ctx: Ctx, db: DemoDb): AnyRecord | null {
  return currentUser(ctx, db)
}

function userIsAdmin(ctx: Ctx, db: DemoDb): AnyRecord | null {
  const user = currentUser(ctx, db)
  return user && user.isAdmin ? user : null
}

function cartDetail(db: DemoDb, userId: number): AnyRecord[] {
  return db.carts
    .filter((cart) => cart.userId === userId)
    .map((cart) => {
      const goods = db.goods.find((g) => g.id === cart.goodsId)
      if (!goods) return null
      return {
        id: cart.id,
        userId: cart.userId,
        goodsId: cart.goodsId,
        quantity: cart.quantity,
        name: goods.name,
        price: goods.price,
        image: goods.image,
        category: goods.category,
        stock: goods.stock,
        description: goods.description,
      }
    })
    .filter(Boolean) as AnyRecord[]
}

function orderDetailOf(db: DemoDb, order: AnyRecord): AnyRecord {
  const user = db.users.find((u) => u.id === order.userId)
  return {
    order,
    items: db.orderItems.filter((item) => item.orderId === order.id),
    username: user ? user.username : '未知用户',
  }
}

function createOrderNo(db: DemoDb): string {
  const stamp = localIso(new Date()).replace(/\D/g, '').slice(0, 14)
  const random = String(Math.floor(Math.random() * 10000)).padStart(4, '0')
  const serial = String(nextId(db, 'order')).padStart(3, '0')
  return `ORD${stamp}${serial}${random}`
}

/** 从购物车生成订单（POST /api/orders） */
function createOrderFromCart(ctx: Ctx, db: DemoDb, user: AnyRecord): ApiResult {
  const body = ctx.body || {}
  const shippingName = String(body.shippingName || '')
  const shippingPhone = String(body.shippingPhone || '')
  const shippingAddress = String(body.shippingAddress || '')

  if (!shippingName || !shippingPhone || !shippingAddress) {
    return fail('请先选择收货地址')
  }

  const rawIds = body.goodsIds
  const goodsIds: number[] = Array.isArray(rawIds)
    ? rawIds.map((v) => toNumber(v)).filter((n) => n > 0)
    : rawIds
      ? [toNumber(rawIds)]
      : []

  const userCart = db.carts.filter((cart) => cart.userId === user.id)
  if (userCart.length === 0) return fail('购物车为空')

  const selected = goodsIds.length > 0
    ? userCart.filter((cart) => goodsIds.includes(Number(cart.goodsId)))
    : userCart

  if (selected.length === 0) return fail('没有选中任何商品')

  let totalAmount = 0
  for (const cart of selected) {
    const goods = db.goods.find((g) => g.id === cart.goodsId)
    if (!goods) return fail('商品不存在')
    if (toNumber(goods.stock) < toNumber(cart.quantity)) {
      return fail(`商品 ${goods.name} 库存不足`)
    }
    totalAmount += toNumber(goods.price) * toNumber(cart.quantity)
  }

  const now = localIso(new Date())
  const order = {
    id: 0,
    userId: user.id,
    orderNo: createOrderNo(db),
    totalAmount: Number(totalAmount.toFixed(2)),
    status: 'pending',
    shippingName,
    shippingPhone,
    shippingAddress,
    paymentMethod: body.paymentMethod || 'wechat',
    createdAt: now,
    updatedAt: now,
    refundStatus: null,
    refundReason: null,
    refundRequestedAt: null,
    refundProcessedAt: null,
    refundNote: null,
  }
  order.id = nextId(db, 'order')
  db.orders.push(order)

  for (const cart of selected) {
    const goods = db.goods.find((g) => g.id === cart.goodsId)!
    db.orderItems.push({
      id: nextId(db, 'orderItem'),
      orderId: order.id,
      goodsId: goods.id,
      goodsName: goods.name,
      price: goods.price,
      quantity: cart.quantity,
      image: goods.image,
      description: goods.description,
      categoryName: goods.category,
      createdAt: now,
    })
    goods.stock = Math.max(0, toNumber(goods.stock) - toNumber(cart.quantity))
    goods.updatedAt = now
    db.carts = db.carts.filter((c) => c !== cart)
  }

  return ok(order, '订单创建成功')
}

/** 立即购买（POST /api/orders/direct） */
function createDirectOrder(ctx: Ctx, db: DemoDb, user: AnyRecord): ApiResult {
  if (user.isAdmin) return fail('管理员不能购买商品')

  const goodsId = toNumber(ctx.query.get('goodsId'))
  const quantity = Math.max(1, toNumber(ctx.query.get('quantity'), 1))
  const paymentMethod = ctx.query.get('paymentMethod') || 'wechat'
  const shippingName = ctx.query.get('shippingName') || ''
  const shippingPhone = ctx.query.get('shippingPhone') || ''
  const shippingAddress = ctx.query.get('shippingAddress') || ''

  const goods = db.goods.find((g) => g.id === goodsId)
  if (!goods) return fail('商品不存在')
  if (toNumber(goods.stock) < quantity) return fail(`商品 ${goods.name} 库存不足`)

  const now = localIso(new Date())
  const order: AnyRecord = {
    id: nextId(db, 'order'),
    userId: user.id,
    orderNo: '',
    totalAmount: Number((toNumber(goods.price) * quantity).toFixed(2)),
    status: 'paid',
    shippingName,
    shippingPhone,
    shippingAddress,
    paymentMethod,
    createdAt: now,
    updatedAt: now,
    refundStatus: null,
    refundReason: null,
    refundRequestedAt: null,
    refundProcessedAt: null,
    refundNote: null,
  }
  // 订单号依赖自增 id，所以先自增再补上
  order.orderNo = `ORD${now.replace(/\D/g, '').slice(0, 14)}${String(order.id).padStart(3, '0')}${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`
  db.orders.push(order)

  db.orderItems.push({
    id: nextId(db, 'orderItem'),
    orderId: order.id,
    goodsId: goods.id,
    goodsName: goods.name,
    price: goods.price,
    quantity,
    image: goods.image,
    description: goods.description,
    categoryName: goods.category,
    createdAt: now,
  })

  goods.stock = Math.max(0, toNumber(goods.stock) - quantity)
  goods.updatedAt = now

  return ok(order, '支付成功')
}

function dashboard(db: DemoDb): ApiResult {
  const today = new Date()
  const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime()

  const todayOrders = db.orders.filter((o) => new Date(o.createdAt).getTime() >= startOfToday)
  const sum = (list: AnyRecord[]) => list.reduce((acc, item) => acc + toNumber(item.totalAmount), 0)

  const days: AnyRecord[] = []
  for (let i = 6; i >= 0; i -= 1) {
    const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() - i)
    const start = d.getTime()
    const end = start + DAY
    const count = db.orders.filter((o) => {
      const t = new Date(o.createdAt).getTime()
      return t >= start && t < end
    }).length
    const label = i === 0 ? '今天' : i === 1 ? '昨天' : `${d.getMonth() + 1}/${d.getDate()}`
    days.push({ day: label, count })
  }
  const maxCount = Math.max(...days.map((d) => d.count), 0)
  days.forEach((d) => {
    let percent = maxCount > 0 ? Math.round((d.count * 100) / maxCount) : 0
    if (percent === 0 && d.count > 0) percent = 10
    d.percent = percent
  })

  return ok({
    todayOrders: todayOrders.length,
    todayRevenue: Number(sum(todayOrders).toFixed(2)),
    totalOrders: db.orders.length,
    totalRevenue: Number(sum(db.orders).toFixed(2)),
    totalUsers: db.users.length,
    lastSevenDays: days,
  })
}

/* ------------------------------------------------------------------ */
/* 路由                                                                */
/* ------------------------------------------------------------------ */

function route(ctx: Ctx): ApiResult {
  const db = loadDb()
  const seg = ctx.segs.slice(1) // 去掉开头的 'api'
  const [resource, second, third] = seg
  const method = ctx.method.toUpperCase()

  /* ------------------------------ 认证 ------------------------------ */
  if (resource === 'auth') {
    if (second === 'login' && method === 'POST') {
      const username = String(ctx.body.username || '').trim()
      const password = String(ctx.body.password || '')
      const user = db.users.find((u) => u.username === username)
      if (!user || user.password !== password) return fail('用户名或密码错误')
      if (user.enabled === false) return fail('该账号已被禁用')
      return ok({ token: tokenOf(user), user: publicUser(user) }, '登录成功')
    }

    if (second === 'register' && method === 'POST') {
      const username = String(ctx.body.username || '').trim()
      const password = String(ctx.body.password || '')
      if (username.length < 3) return fail('用户名长度必须在3-50之间')
      if (password.length < 6) return fail('密码长度必须在6-100之间')
      if (db.users.some((u) => u.username === username)) return fail('用户名已存在')

      const user: AnyRecord = {
        id: nextId(db, 'user'),
        username,
        password,
        email: ctx.body.email || `${username}@example.com`,
        phone: ctx.body.phone || '',
        enabled: true,
        isAdmin: false,
        role: 'user',
        createdAt: localIso(new Date()),
      }
      db.users.push(user)
      saveDb()
      return ok({ token: tokenOf(user), user: publicUser(user) }, '注册成功')
    }

    const me = requireUser(ctx, db)
    if (second === 'me' && method === 'GET') {
      if (!me) return fail('请先登录', 401)
      return ok(publicUser(me))
    }
    if (second === 'me' && method === 'PUT') {
      if (!me) return fail('请先登录', 401)
      if (ctx.body.email) me.email = ctx.body.email
      if (ctx.body.phone) me.phone = ctx.body.phone
      if (ctx.body.password) me.password = ctx.body.password
      saveDb()
      return ok(publicUser(me), '更新成功')
    }
    if (second === 'delete' && method === 'DELETE') {
      if (!me) return fail('请先登录', 401)
      if (me.username === 'admin') return fail('主管理员不可删除')
      db.users = db.users.filter((u) => u.id !== me.id)
      db.carts = db.carts.filter((c) => c.userId !== me.id)
      db.orders = db.orders.filter((o) => o.userId !== me.id)
      db.favorites = db.favorites.filter((f) => f.userId !== me.id)
      db.addresses = db.addresses.filter((a) => a.userId !== me.id)
      saveDb()
      return ok(null, '账号注销成功')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 商品 ------------------------------ */
  if (resource === 'goods') {
    if (!second && method === 'GET') {
      const category = ctx.query.get('category') || ''
      const keyword = (ctx.query.get('keyword') || '').trim()
      let list = db.goods.slice()
      if (keyword) {
        list = list.filter((g) => String(g.name).includes(keyword) || String(g.description || '').includes(keyword))
      } else if (category) {
        list = list.filter((g) => g.category === category)
      }
      return ok(list)
    }

    if (!second && method === 'POST') {
      const admin = userIsAdmin(ctx, db)
      if (!admin) return fail('需要管理员权限', 403)
      const name = String(ctx.body.name || '').trim()
      const price = toNumber(ctx.body.price)
      if (!name) return fail('商品名称不能为空')
      if (price <= 0) return fail('商品价格必须为正数')
      const goods: AnyRecord = {
        id: nextId(db, 'goods'),
        name,
        price: Number(price.toFixed(2)),
        stock: toNumber(ctx.body.stock),
        category: ctx.body.category || '未分类',
        description: ctx.body.description || '',
        image: ctx.body.image || imageUrl('jpyw.jpg'),
        status: ctx.body.status || 'active',
        sales: 0,
        createdAt: localIso(new Date()),
        updatedAt: null,
      }
      db.goods.push(goods)
      saveDb()
      return ok(goods, '商品创建成功')
    }

    if (second && method === 'GET') {
      const goods = db.goods.find((g) => g.id === toNumber(second))
      if (!goods) return fail('商品不存在')
      return ok(goods)
    }

    if (second && method === 'PUT') {
      const admin = userIsAdmin(ctx, db)
      if (!admin) return fail('需要管理员权限', 403)
      const goods = db.goods.find((g) => g.id === toNumber(second))
      if (!goods) return fail('商品不存在')
      const body = ctx.body
      if (body.name !== undefined) goods.name = String(body.name)
      if (body.price !== undefined) goods.price = Number(toNumber(body.price).toFixed(2))
      if (body.stock !== undefined) goods.stock = toNumber(body.stock)
      if (body.category !== undefined) goods.category = body.category
      if (body.description !== undefined) goods.description = body.description
      if (body.image !== undefined) goods.image = body.image
      if (body.status !== undefined) goods.status = body.status
      goods.updatedAt = localIso(new Date())
      saveDb()
      return ok(goods, '商品更新成功')
    }

    if (second && method === 'DELETE') {
      const admin = userIsAdmin(ctx, db)
      if (!admin) return fail('需要管理员权限', 403)
      const id = toNumber(second)
      if (!db.goods.some((g) => g.id === id)) return fail('商品不存在')
      db.goods = db.goods.filter((g) => g.id !== id)
      db.carts = db.carts.filter((c) => c.goodsId !== id)
      db.favorites = db.favorites.filter((f) => f.goodsId !== id)
      saveDb()
      return ok(null, '商品已下架')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 购物车 ---------------------------- */
  if (resource === 'cart') {
    const user = requireUser(ctx, db)
    if (!user) return fail('请先登录', 401)

    if (!second && method === 'GET') return ok(cartDetail(db, user.id))

    if (!second && method === 'POST') {
      const goodsId = toNumber(ctx.body.goodsId)
      const quantity = Math.max(1, toNumber(ctx.body.quantity, 1))
      const goods = db.goods.find((g) => g.id === goodsId)
      if (!goods) return fail('商品不存在')

      const existing = db.carts.find((c) => c.userId === user.id && c.goodsId === goodsId)
      const nextQuantity = (existing ? toNumber(existing.quantity) : 0) + quantity
      if (toNumber(goods.stock) < nextQuantity) return fail('库存不足')

      if (existing) {
        existing.quantity = nextQuantity
        existing.updatedAt = localIso(new Date())
        saveDb()
        return ok(existing, '添加成功')
      }
      const cart = {
        id: nextId(db, 'cart'),
        userId: user.id,
        goodsId,
        quantity,
        createdAt: localIso(new Date()),
        updatedAt: null,
      }
      db.carts.push(cart)
      saveDb()
      return ok(cart, '添加成功')
    }

    if (!second && method === 'PUT') {
      const goodsId = toNumber(ctx.body.goodsId)
      const quantity = Math.max(1, toNumber(ctx.body.quantity, 1))
      const goods = db.goods.find((g) => g.id === goodsId)
      if (!goods) return fail('商品不存在')
      if (toNumber(goods.stock) < quantity) return fail('库存不足')
      const existing = db.carts.find((c) => c.userId === user.id && c.goodsId === goodsId)
      if (!existing) return fail('购物车中没有该商品')
      existing.quantity = quantity
      existing.updatedAt = localIso(new Date())
      saveDb()
      return ok(existing, '更新成功')
    }

    if (!second && method === 'DELETE') {
      db.carts = db.carts.filter((c) => c.userId !== user.id)
      saveDb()
      return ok(null, '购物车已清空')
    }

    if (second && method === 'DELETE') {
      const goodsId = toNumber(second)
      db.carts = db.carts.filter((c) => !(c.userId === user.id && c.goodsId === goodsId))
      saveDb()
      return ok(null, '删除成功')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 收藏 ------------------------------ */
  if (resource === 'favorites') {
    const user = requireUser(ctx, db)
    if (!user) return fail('请先登录', 401)

    if (!second && method === 'GET') {
      return ok(db.favorites.filter((f) => f.userId === user.id))
    }
    if (second && method === 'POST') {
      const goodsId = toNumber(second)
      if (!db.goods.some((g) => g.id === goodsId)) return fail('商品不存在')
      if (db.favorites.some((f) => f.userId === user.id && f.goodsId === goodsId)) {
        return fail('已经收藏过该商品')
      }
      const favorite = { id: nextId(db, 'favorite'), userId: user.id, goodsId, createdAt: localIso(new Date()) }
      db.favorites.push(favorite)
      saveDb()
      return ok(favorite, '收藏成功')
    }
    if (second && method === 'DELETE') {
      const goodsId = toNumber(second)
      db.favorites = db.favorites.filter((f) => !(f.userId === user.id && f.goodsId === goodsId))
      saveDb()
      return ok(null, '取消收藏成功')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 地址 ------------------------------ */
  if (resource === 'addresses') {
    const user = requireUser(ctx, db)
    if (!user) return fail('请先登录', 401)

    if (!second && method === 'GET') {
      return ok(db.addresses.filter((a) => a.userId === user.id))
    }

    if (!second && method === 'POST') {
      const body = ctx.body
      if (!body.name || !body.phone || !body.address) return fail('收货人、电话和详细地址不能为空')
      const address: AnyRecord = {
        id: nextId(db, 'address'),
        userId: user.id,
        name: body.name,
        phone: body.phone,
        province: body.province || '',
        city: body.city || '',
        district: body.district || '',
        address: body.address,
        isDefault: !!body.isDefault,
        createdAt: localIso(new Date()),
        updatedAt: null,
      }
      if (address.isDefault) {
        db.addresses.forEach((a) => {
          if (a.userId === user.id) a.isDefault = false
        })
      }
      db.addresses.push(address)
      saveDb()
      return ok(address, '地址添加成功')
    }

    if (second && method === 'GET') {
      const address = db.addresses.find((a) => a.id === toNumber(second))
      if (!address) return fail('地址不存在')
      if (address.userId !== user.id) return fail('无权访问该地址')
      return ok(address)
    }

    if (second && method === 'PUT') {
      const address = db.addresses.find((a) => a.id === toNumber(second))
      if (!address) return fail('地址不存在')
      if (address.userId !== user.id) return fail('无权修改该地址')
      const body = ctx.body
      ;['name', 'phone', 'province', 'city', 'district', 'address'].forEach((key) => {
        if (body[key] !== undefined) address[key] = body[key]
      })
      if (body.isDefault !== undefined) address.isDefault = !!body.isDefault
      if (address.isDefault) {
        db.addresses.forEach((a) => {
          if (a.userId === user.id && a.id !== address.id) a.isDefault = false
        })
      }
      address.updatedAt = localIso(new Date())
      saveDb()
      return ok(address, '地址更新成功')
    }

    if (second && method === 'DELETE') {
      const address = db.addresses.find((a) => a.id === toNumber(second))
      if (!address) return fail('地址不存在')
      if (address.userId !== user.id) return fail('无权删除该地址')
      db.addresses = db.addresses.filter((a) => a.id !== address.id)
      saveDb()
      return ok(null, '地址删除成功')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 订单 ------------------------------ */
  if (resource === 'orders') {
    const user = requireUser(ctx, db)
    if (!user) return fail('请先登录', 401)

    if (!second && method === 'GET') {
      const list = db.orders
        .filter((o) => o.userId === user.id)
        .sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)))
        .map((o) => orderDetailOf(db, o))
      return ok(list)
    }

    if (!second && method === 'POST') return createOrderFromCart(ctx, db, user)

    if (second === 'direct' && method === 'POST') return createDirectOrder(ctx, db, user)

    const orderId = toNumber(second)

    if (third === 'refund' && method === 'POST') {
      const order = db.orders.find((o) => o.id === orderId)
      if (!order) return fail('订单不存在')
      if (order.userId !== user.id) return fail('只能申请自己订单的退款')
      if (order.refundStatus === 'refund_pending' || order.refundStatus === 'refund_approved') {
        return fail('该订单已在退款流程中')
      }
      const reason = String(ctx.body.reason || '').trim()
      if (!reason) return fail('请填写退款原因')
      order.refundStatus = 'refund_pending'
      order.refundReason = reason
      order.refundRequestedAt = localIso(new Date())
      order.updatedAt = order.refundRequestedAt
      saveDb()
      return ok(order, '退款申请已提交')
    }

    if (third === 'receive' && method === 'PUT') {
      const order = db.orders.find((o) => o.id === orderId)
      if (!order) return fail('订单不存在')
      if (order.userId !== user.id) return fail('只能确认自己订单的收货')
      if (order.status !== 'shipped') return fail('该订单状态不支持确认收货')
      order.status = 'completed'
      order.updatedAt = localIso(new Date())
      saveDb()
      return ok(order, '确认收货成功')
    }

    if (third === 'status' && method === 'PUT') {
      const order = db.orders.find((o) => o.id === orderId)
      if (!order) return fail('订单不存在')
      order.status = ctx.query.get('status') || order.status
      order.updatedAt = localIso(new Date())
      saveDb()
      return ok(order, '状态更新成功')
    }

    if (!third && method === 'GET') {
      const order = db.orders.find((o) => o.id === orderId)
      if (!order) return fail('订单不存在')
      return ok(order)
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 评价 ------------------------------ */
  if (resource === 'reviews') {
    if (second === 'goods' && third && method === 'GET') {
      const productId = toNumber(third)
      const list = db.reviews
        .filter((r) => r.productId === productId)
        .sort((a, b) => String(b.submittedAt).localeCompare(String(a.submittedAt)))
      return ok(list)
    }

    if (!second && method === 'POST') {
      const user = requireUser(ctx, db)
      if (!user) return fail('需要登录后才可评价', 401)
      const productId = toNumber(ctx.body.goodsId)
      const content = String(ctx.body.content || '').trim()
      if (!productId) return fail('商品不存在')
      if (!content) return fail('评价内容不能为空')
      let rating = toNumber(ctx.body.rating, 5)
      if (rating < 1 || rating > 5) rating = 5
      const now = localIso(new Date())
      const name = String(user.username)
      const masked = name.length > 2 ? `${name.slice(0, 2)}**` : `${name}**`
      const review: AnyRecord = {
        id: nextId(db, 'review'),
        productId,
        customerId: user.id,
        customerName: masked,
        starCount: rating,
        rating,
        reviewText: content,
        content,
        submittedAt: now,
        createdAt: now,
      }
      review.reviewId = review.id
      db.reviews.push(review)
      saveDb()
      return ok(review, '评价已提交')
    }

    if (second === 'mine' && method === 'GET') {
      const user = requireUser(ctx, db)
      if (!user) return fail('请先登录', 401)
      return ok(db.reviews.filter((r) => r.customerId === user.id))
    }

    if (second && method === 'DELETE') {
      const user = requireUser(ctx, db)
      if (!user) return fail('请先登录', 401)
      const reviewId = toNumber(second)
      db.reviews = db.reviews.filter((r) => !(r.id === reviewId && r.customerId === user.id))
      saveDb()
      return ok(null, '已移除')
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 反馈 ------------------------------ */
  if (resource === 'feedback') {
    if (!second && method === 'POST') {
      const user = requireUser(ctx, db)
      const body = ctx.body
      if (!body.type || !body.title || !body.content) return fail('类型、标题和内容都不能为空')
      const feedback: AnyRecord = {
        id: nextId(db, 'feedback'),
        userId: user ? user.id : null,
        username: user ? user.username : 'guest',
        type: body.type,
        title: body.title,
        content: body.content,
        contact: body.contact || '',
        isProcessed: false,
        processedNote: null,
        createdAt: localIso(new Date()),
        processedAt: null,
      }
      db.feedbacks.push(feedback)
      saveDb()
      return ok(feedback, '提交成功')
    }

    if (second === 'my' && method === 'GET') {
      const user = requireUser(ctx, db)
      if (!user) return fail('请先登录', 401)
      return ok(db.feedbacks.filter((f) => f.userId === user.id))
    }
    return fail('接口不存在', 404)
  }

  /* ------------------------------ 管理端 ---------------------------- */
  if (resource === 'admin') {
    // 登录接口不需要鉴权
    if (second === 'login' && method === 'POST') {
      const username = String(ctx.body.username || '').trim()
      const password = String(ctx.body.password || '')
      const user = db.users.find((u) => u.username === username)
      if (!user) return fail('管理员账号不存在')
      if (!user.isAdmin) return fail('该账号不是管理员')
      if (user.password !== password) return fail('密码错误')
      return ok({ token: tokenOf(user), user: publicUser(user) }, '登录成功')
    }

    const admin = userIsAdmin(ctx, db)
    if (!admin) return fail('需要管理员权限', 403)

    if (second === 'dashboard' && method === 'GET') return dashboard(db)

    if (second === 'users') {
      if (!third && method === 'GET') {
        return ok(db.users.map((u) => ({ ...publicUser(u), password: '******' })))
      }
      if (third && seg[3] === 'status' && method === 'PUT') {
        const user = db.users.find((u) => u.id === toNumber(third))
        if (!user) return fail('用户不存在')
        if (user.username === 'admin') return fail('主管理员不可禁用')
        user.enabled = ctx.body.enabled !== undefined ? !!ctx.body.enabled : !user.enabled
        saveDb()
        return ok(publicUser(user), '用户状态已更新')
      }
      if (third && method === 'DELETE') {
        const user = db.users.find((u) => u.id === toNumber(third))
        if (!user) return fail('用户不存在')
        if (user.username === 'admin') return fail('主管理员不可删除')
        db.users = db.users.filter((u) => u.id !== user.id)
        db.carts = db.carts.filter((c) => c.userId !== user.id)
        db.favorites = db.favorites.filter((f) => f.userId !== user.id)
        db.addresses = db.addresses.filter((a) => a.userId !== user.id)
        saveDb()
        return ok(null, '用户已删除')
      }
    }

    if (second === 'orders') {
      if (!third && method === 'GET') {
        const list = db.orders.slice().sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt)))
        return ok(list)
      }
      const order = db.orders.find((o) => o.id === toNumber(third))
      if (third && !order) return fail('订单不存在')

      if (third && seg[3] === 'status' && method === 'PUT') {
        order!.status = ctx.query.get('status') || order!.status
        order!.updatedAt = localIso(new Date())
        saveDb()
        return ok(order, '订单状态已更新')
      }

      if (third && seg[3] === 'refund' && method === 'PUT') {
        if (order!.refundStatus !== 'refund_pending') return fail('该订单没有待处理的退款申请')
        const approved = ctx.body.approved
        if (approved === undefined || approved === null) return fail('请指定是否通过退款')
        order!.refundStatus = approved ? 'refund_approved' : 'refund_rejected'
        order!.refundNote = ctx.body.note || (approved ? '同意退款' : '拒绝退款')
        order!.refundProcessedAt = localIso(new Date())
        order!.updatedAt = order!.refundProcessedAt
        saveDb()
        return ok(order, approved ? '已通过退款' : '已拒绝退款')
      }

      if (third && seg[3] === 'detail' && method === 'GET') {
        return ok(orderDetailOf(db, order!))
      }

      if (third && method === 'DELETE') {
        db.orders = db.orders.filter((o) => o.id !== order!.id)
        db.orderItems = db.orderItems.filter((item) => item.orderId !== order!.id)
        saveDb()
        return ok(null, '订单已删除')
      }
    }

    if (second === 'feedbacks') {
      if (!third && method === 'GET') {
        return ok(db.feedbacks.slice().sort((a, b) => String(b.createdAt).localeCompare(String(a.createdAt))))
      }
      const feedback = db.feedbacks.find((f) => f.id === toNumber(third))
      if (third && !feedback) return fail('反馈不存在')

      if (third && seg[3] === 'process' && method === 'PUT') {
        feedback!.isProcessed = true
        feedback!.processedNote = ctx.body.note || feedback!.processedNote || '已处理'
        feedback!.processedAt = localIso(new Date())
        saveDb()
        return ok(feedback, '反馈已处理')
      }

      if (third && method === 'DELETE') {
        db.feedbacks = db.feedbacks.filter((f) => f.id !== feedback!.id)
        saveDb()
        return ok(null, '反馈已删除')
      }
    }

    if (second === 'subadmin' && method === 'POST') {
      const username = String(ctx.body.username || '').trim()
      const password = String(ctx.body.password || '')
      if (!username || !password) return fail('用户名和密码不能为空')
      if (password.length < 6) return fail('密码长度不能少于6位')
      if (db.users.some((u) => u.username === username)) return fail('用户名已存在')
      const subAdmin: AnyRecord = {
        id: nextId(db, 'user'),
        username,
        password,
        email: ctx.body.email || `${username}@example.com`,
        phone: ctx.body.phone || '',
        enabled: true,
        isAdmin: true,
        role: 'subadmin',
        createdAt: localIso(new Date()),
      }
      db.users.push(subAdmin)
      saveDb()
      return ok(publicUser(subAdmin), '子管理员创建成功')
    }

    if (second === 'password' && method === 'PUT') {
      const oldPassword = String(ctx.body.oldPassword || '')
      const newPassword = String(ctx.body.newPassword || '')
      if (!oldPassword || !newPassword) return fail('旧密码和新密码都不能为空')
      if (admin.password !== oldPassword) return fail('旧密码不正确')
      if (newPassword.length < 6) return fail('密码长度不能少于6位')
      admin.password = newPassword
      saveDb()
      return ok(null, '密码修改成功')
    }

    return fail('接口不存在', 404)
  }

  return fail('接口不存在', 404)
}

/* ------------------------------------------------------------------ */
/* 请求拦截                                                            */
/* ------------------------------------------------------------------ */

const isApiUrl = (url: string): boolean => {
  if (!url) return false
  try {
    const parsed = new URL(url, window.location.href)
    if (API_HOSTS.includes(parsed.host)) return true
    if (parsed.origin === window.location.origin && parsed.pathname.startsWith('/api/')) return true
  } catch {
    return false
  }
  return /^\/api\//.test(url) || /^api\//.test(url)
}

function headersToObject(headers: any): Record<string, string> {
  const result: Record<string, string> = {}
  if (!headers) return result
  try {
    if (typeof headers.forEach === 'function' && !Array.isArray(headers)) {
      headers.forEach((value: string, key: string) => {
        result[String(key).toLowerCase()] = value
      })
      return result
    }
    if (Array.isArray(headers)) {
      headers.forEach((pair: [string, string]) => {
        result[String(pair[0]).toLowerCase()] = pair[1]
      })
      return result
    }
    Object.keys(headers).forEach((key) => {
      result[key.toLowerCase()] = String(headers[key])
    })
  } catch {
    /* ignore */
  }
  return result
}

/** 把请求体解析成对象；重复的 key（如 goodsIds）转成数组 */
function parseBody(init: any): AnyRecord {
  const body = init && init.body
  if (body == null) return {}

  const collect = (entries: IterableIterator<[string, string]>): AnyRecord => {
    const result: AnyRecord = {}
    for (const [key, value] of entries) {
      if (Object.prototype.hasOwnProperty.call(result, key)) {
        const prev = result[key]
        result[key] = Array.isArray(prev) ? [...prev, value] : [prev, value]
      } else {
        result[key] = value
      }
    }
    return result
  }

  try {
    if (typeof body === 'string') {
      const text = body.trim()
      if (!text) return {}
      if (text.startsWith('{') || text.startsWith('[')) return JSON.parse(text)
      return collect(new URLSearchParams(text).entries())
    }
    if (typeof URLSearchParams !== 'undefined' && body instanceof URLSearchParams) {
      return collect(body.entries())
    }
    if (typeof FormData !== 'undefined' && body instanceof FormData) {
      return collect(body.entries() as IterableIterator<[string, string]>)
    }
  } catch {
    return {}
  }
  return {}
}

function buildContext(input: any, init: any): Ctx | null {
  const url = typeof input === 'string' ? input : input && typeof input.url === 'string' ? input.url : ''
  if (!url || !isApiUrl(url)) return null

  const parsed = new URL(url, window.location.href)
  const method = String((init && init.method) || (input && input.method) || 'GET').toUpperCase()

  return {
    method,
    path: parsed.pathname,
    segs: parsed.pathname.split('/').filter(Boolean),
    query: parsed.searchParams,
    headers: headersToObject((init && init.headers) || (input && input.headers)),
    body: parseBody(init),
  }
}

/** 是否启用演示后端 */
export function isDemoEnabled(): boolean {
  try {
    const params = new URLSearchParams(window.location.search)
    const flag = params.get('demo')
    if (flag === 'off' || flag === '0') return false
    if (flag === 'on' || flag === '1') return true
    if (localStorage.getItem('demoMode') === 'off') return false
  } catch {
    /* ignore */
  }
  if (import.meta.env.VITE_DEMO === 'off') return false
  if (import.meta.env.VITE_DEMO === 'on') return true
  return import.meta.env.PROD
}

export function installDemoBackend(): void {
  if (typeof window === 'undefined') return
  const w = window as any
  if (w.__demoBackendInstalled) return
  if (!isDemoEnabled()) return
  if (typeof w.fetch !== 'function') return

  const nativeFetch = w.fetch.bind(w)
  w.__demoBackendInstalled = true
  w.__demoBackendNativeFetch = nativeFetch
  w.__demoReset = resetDemoData
  w.__demoDb = () => loadDb()

  w.fetch = (input: any, init?: any): Promise<Response> => {
    const ctx = buildContext(input, init)
    if (!ctx) return nativeFetch(input, init)
    try {
      return Promise.resolve(makeResponse(route(ctx)))
    } catch (error) {
      console.error('[demo-backend] 处理请求失败:', error)
      return Promise.resolve(makeResponse(fail('演示后端处理异常')))
    }
  }

  // 提前把种子数据写进 localStorage，保证首屏就有商品
  loadDb()
}
