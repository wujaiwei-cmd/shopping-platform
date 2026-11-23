# 网络购物平台

基于 Vue3 + Spring Boot 的现代化网络购物平台系统，支持用户端和管理端双端操作，实现完整的购物流程。

## 技术栈

### 前端技术
- **框架**: Vue 3.5.32 (Composition API)
- **语言**: TypeScript
- **路由**: Vue Router 5.0.4
- **UI组件库**: Element Plus 2.13.7
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
- 数据存在 `localStorage`（键名 `shopping_demo_db_v2`），每个浏览器一份，刷新不丢
- 接口路径 / 请求参数 / 返回字段与 `backend/` 里的 Controller 一一对应

### 演示账号

| 角色 | 账号 | 密码 | 入口 |
| --- | --- | --- | --- |
| 普通用户 | `demo` | `demo123` | 首页 → 登录 |
| 管理员 | `admin` | `admin123` | 首页右上角「管理后台」或 `/admin/login` |

登录页也可以点「立即注册」新建账号。

### 在线可以走通的流程

- **首页**：轮播图、分类筛选（点左侧分类文字）、搜索、商品详情、加入购物车/立即购买、收藏、评价
- **购物车**：加入、数量增减（会真正落库）、删除、结算下单
- **订单**：我的订单、订单详情、确认收货、申请退款
- **个人中心**：收货地址增删改、收藏夹、帮助中心（提交反馈 / 申请退款）
- **管理端**：数据概览（近七天趋势）、商品增删改、订单状态与退款审核、用户禁用/删除、反馈处理、添加子管理员

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