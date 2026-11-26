# 网络购物平台

基于 Vue3 + Spring Boot 的现代化网络购物平台系统，支持用户端和管理端双端操作，实现完整的购物流程。

## 技术栈

### 前端技术
- **框架**: Vue 3.5.32 (Composition API)
- **语言**: TypeScript
- **路由**: Vue Router 5.0.4
- **UI组件库**: Element Plus 2.13.7（仅购物车用到 message/empty；为了首屏少下载 1MB+，已换成自写的轻量 toast 与空状态）
- **构建工具**: Vite 8.0.4

### 后端技术
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA + Hibernate
- **安全认证**: Spring Security + JWT (jjwt 0.12.3)
- **代码简化**: Lombok

## 功能模块

### 用户端
- 首页商品浏览（轮播图、分类导航、猜你喜欢）
- 商品详情查看与购买
- 购物车管理（添加、删除、数量调整、结算）
- 订单管理（下单、支付、订单状态追踪、退款售后）
- 个人中心（用户信息、收货地址、收藏夹、帮助中心）

### 管理端
- 控制台数据概览
- 商品管理（新增、编辑、删除、状态管理）
- 订单管理（订单列表、状态变更、退款处理）
- 用户管理（用户列表、权限管理、禁用/启用）
- 反馈管理（反馈列表、处理状态、删除）

## 在线演示（无需后端）

🌐 线上地址：<https://wujaiwei-cmd.github.io/shopping-platform/>

GitHub Pages 只能托管静态文件，跑不了 Spring Boot + MySQL。为了让「别人打开就能用」，
前端内置了一层浏览器端演示后端：`src/api/demoBackend.ts`。

- 拦截发往 `http://localhost:8081/api/*` 的请求，在浏览器里完成全部业务逻辑
- 数据存在 `localStorage`（键名 `shopping_demo_db_v3`），每个浏览器一份，刷新不丢
- 接口路径 / 请求参数 / 返回字段与 `backend/` 里的 Controller 一一对应
- **初始没有任何商品**，商品只能由管理员在「商品管理」里添加

### 演示账号

| 角色 | 账号 | 密码 | 入口 |
| --- | --- | --- | --- |
| 普通用户 | `demo` | `demo123` | 首页 → 登录 |
| 管理员 | `admin` | `admin123` | 首页右上角「管理后台」或 `/admin/login` |

登录页也可以点「立即注册」新建账号。

> 第一次打开线上站点时，首页会显示「店铺里还没有商品」。
> 用 `admin / admin123` 进入「管理后台 → 商品管理 → 添加商品」，
> 填好名称 / 价格 / 库存 / 分类保存后，首页的「热门商品」「今日特惠」就会立刻出现这件商品，
> 普通用户才能加购物车、下单、评价。

### 发布商品（让别人也能看到）

商品目录就是一个文件：[`shopping/public/products.json`](public/products.json)。
访客打开站点时直接读它，所以**只要文件里有商品，所有访客都能看到**。

流程：

1. 用 `admin / admin123` 登录 → **管理后台 → 商品管理 → 添加商品**（这一步只改当前浏览器里的“草稿”）
2. 页面顶部点 **「发布到线上」**，二选一：
   - **已经设置过 GitHub Token**：直接提交到仓库，Pages 约 1 分钟后自动重建；
   - **没设置 Token**：会复制一份 JSON，并打开 GitHub 上的编辑页，粘贴 → 提交即可。
3. 发布后，任何人打开链接都能看到这些商品，并能加购、下单。

#### 想一键发布？（可选，2 分钟配置一次）

1. 打开 <https://github.com/settings/tokens?type=beta> 新建 **Fine-grained token**
2. Repository access 选 **Only select repositories** → `shopping-platform`
3. Permissions → Repository permissions → **Contents: Read and write**（其余留空）
4. 生成后复制，回到站点的「商品管理」→ 点 **「设置 Token」**，粘贴保存

Token 只存在你本机浏览器的 localStorage 里，不会写进代码或仓库；
它只对这个仓库的文件有写权限，随时可以在 GitHub 上撤销。

#### 首页轮播图

轮播图不再写死，由商品管理里的开关决定：

- 在「添加商品 / 编辑商品」里勾选 **「在首页轮播图展示这件商品」**；
- 可以额外填一个 **轮播图图片链接**（不填就用商品自己的图片）；
- 发布后首页轮播就按勾选的商品显示，点轮播图可以直接进商品详情；
- 一件都没勾选时，自动取前 5 件商品做轮播；店铺没有任何商品则不显示轮播。

> 首页右上角不再放「管理后台」入口（顾客视角更干净），
> 商家直接访问 `/shopping-platform/admin/login` 登录即可。

#### 说明

- 商品目录里只存商品（名称、价格、库存、分类、图片），不涉及用户、订单等隐私数据。
- 本地草稿在发布前只对你自己可见；别人打开看到的始终是 `products.json` 里的内容。
- 图片填 URL 即可（也可以用仓库里 `public/images/` 下的图片，例如
  `/shopping-platform/images/ysfwj.jpg`）。

### 在线可以走通的流程

- **首页**：轮播图、分类筛选（点左侧分类文字）、搜索、商品详情、加入购物车/立即购买、收藏、评价
- **购物车**：加入、数量增减（会真正落库）、删除、结算下单
- **订单**：我的订单、订单详情、确认收货、申请退款
- **个人中心**：收货地址增删改、收藏夹、帮助中心（提交反馈 / 申请退款）
- **管理端**：数据概览（近七天趋势）、商品增删改、订单状态与退款审核、用户禁用/删除、反馈处理、添加子管理员

### 店铺信息与合规页（site.json）

店铺名、公告、客服方式、页脚政策文案都集中在一个文件里：[`public/site.json`](public/site.json)。
改完提交即可生效，不需要动代码：

```json
{
  "storeName": "喜乐严选",
  "slogan": "产地直供 · 品质好物",
  "notice": "新店开业：全场满 99 元包邮 · 48 小时内发货 · 支持 7 天无理由退换",
  "service": { "wechat": "xile-service", "phone": "400-000-0000", "email": "service@example.com", "hours": "每天 9:00 - 21:00" },
  "footer": { "about": "…", "shipping": "…", "refund": "…", "privacy": "…", "icp": "备案号，可留空" }
}
```

用到它的位置：

- 顶部店名（首页与「我的 / 购物车 / 收藏夹 / 帮助中心」）
- 首页顶部的店铺公告条
- 页脚：关于我们 / 配送与运费 / 退换货政策 / 隐私政策（点开是弹窗正文）
- 右下角「客服」浮窗：微信、电话、邮箱、服务时间
- 页面标题（店铺名 + 标语）

### 商业化细节

- **商品排序**：综合 / 价格从低到高 / 价格从高到低 / 最新上架，另有「仅看有货」开关
- **SEO**：标题与描述、Open Graph 分享卡片、JSON-LD 店铺结构化数据、`robots.txt`、`sitemap.xml`
- **可安装**：`site.webmanifest`，手机浏览器可「添加到主屏幕」
- **首屏**：内联加载骨架；弱网下先看到品牌页再出内容
- **管理入口**：`/admin/login`（前台不再暴露管理入口）

### 关闭演示模式（连本机真实后端）

启动 `backend/` 后，任选一种方式关闭演示后端，请求就会照常发往 `http://localhost:8081`：

```text
# 1. URL 参数
https://wujaiwei-cmd.github.io/shopping-platform/?demo=off

# 2. 控制台
localStorage.demoMode = 'off'   // 恢复：localStorage.removeItem('demoMode')
```

控制台执行 `__demoReset()` 可以清空并重置演示数据。

## 项目结构

```
网络购物平台/
├── shopping/              # 前端项目
│   ├── src/
│   │   ├── components/    # Vue组件
│   │   ├── router/        # 路由配置
│   │   ├── App.vue        # 根组件
│   │   ├── main.ts        # 入口文件
│   │   └── style.css      # 全局样式
│   ├── index.html         # HTML模板
│   ├── vite.config.ts     # Vite配置
│   └── package.json       # 前端依赖
├── backend/               # 后端项目
│   ├── src/main/java/com/example/shopping/
│   │   ├── controller/    # 控制器层
│   │   ├── service/       # 服务层
│   │   ├── repository/    # 数据访问层
│   │   ├── entity/        # 实体类
│   │   ├── dto/           # 数据传输对象
│   │   ├── security/      # 安全配置
│   │   └── config/        # 配置类
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── data.sql      # 初始化数据
│   └── pom.xml           # Maven配置
└── logs/                 # 日志文件目录
```

## 快速开始

### 环境要求
- Node.js 18+
- JDK 17+
- MySQL 8.0+

### 后端启动
1. 创建数据库 `shopping`
2. 修改 `backend/src/main/resources/application.yml` 中的数据库配置
3. 运行后端服务:
 mvn spring-boot:run
### 前端启动
1. 安装依赖:
   ```bash
   cd shopping
   npm install
   ```
2. 启动开发服务器:
   ```bash
   npm run dev
   ```

### 构建生产版本
```bash
cd shopping
npm run build
```

## 默认账号

### 管理员账号
- **用户名**: admin
- **密码**: admin123

### 访问地址
- 前端用户端: http://localhost:5173/
- 前端管理端: http://localhost:5173/admin/login
- 后端API: http://localhost:8080/api/

## 项目特点

- 前后端分离架构，职责清晰
- 响应式设计，支持PC端和移动端
- JWT无状态认证，安全性高
- RESTful API设计，易于扩展
- 完整的购物流程闭环