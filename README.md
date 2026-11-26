# 网络购物平台

基于 Vue 3 + Spring Boot 的全栈购物平台，用户端 + 管理端双端齐全，支持完整的购物 / 下单 / 退款 / 评价流程。

📦 **源码仓库**：<https://github.com/wujaiwei-cmd/shopping-platform>
🌐 **在线预览**：<https://wujaiwei-cmd.github.io/shopping-platform/>

> **在线预览现在可以直接点着用**：GitHub Pages 跑不了 Java 后端，所以前端内置了一层
> 浏览器端演示后端（`shopping/src/api/demoBackend.ts`），用 localStorage 模拟全部接口，
> 登录 / 购物车 / 下单 / 退款 / 收藏 / 地址 / 评价 / 反馈 / 管理端都能真跑一遍。
>
> - 普通用户：`demo` / `demo123`
> - 管理员：`admin` / `admin123`（首页右上角「管理后台」进入）
> - **线上初始没有商品**：先以管理员身份到「商品管理」添加，再点 **「发布到线上」**
>   （提交到仓库里的 `shopping/public/products.json`），所有访客才看得到
> - 用户 / 订单 / 购物车等数据只在各自浏览器里；控制台执行 `__demoReset()` 可重置
> - 想连本机真实后端：访问 `?demo=off`，或执行 `localStorage.demoMode = 'off'` 后刷新

---

## 🚀 快速启动（3 步跑起来）

> 前提：电脑上已经装好 **JDK 17+**、**Maven 3.9+**、**Node.js 18+**、**MySQL 8.0+**。
> 没装的话先看文末「环境准备」。

### 第 1 步：建数据库

打开命令行，执行两条 SQL 脚本（会让你输入 MySQL root 密码）：

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/data.sql
```

> 执行完会自动创建 `shopping` 库、10 张表，以及默认管理员账号。

### 第 2 步：配置数据库连接

数据库连接默认是 `root` / `123456`。如果你的 MySQL 密码不是这个，**推荐用环境变量覆盖**，
而不是改代码（改过的配置文件容易误提交）：

```bash
# Windows PowerShell
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "你的密码"
$env:JWT_SECRET   = "至少32位的随机字符串"

# macOS / Linux
export DB_USERNAME=root
export DB_PASSWORD=你的密码
export JWT_SECRET=至少32位的随机字符串
```

也可以直接编辑 `backend/src/main/resources/application.yml`，对应的占位符是：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/shopping?...}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}

jwt:
  secret: ${JWT_SECRET:change_me_to_a_random_secret_at_least_32_chars}
```

> `:` 后面是没设置环境变量时的默认值，所以默认配置下直接启动也能跑。

### 第 3 步：启动后端 + 前端

**开一个终端，启动后端：**

```bash
cd backend
mvn spring-boot:run
```

看到控制台输出 `Started ShoppingApplication` 就说明后端起来了（端口 **8081**）。

**再开一个终端，启动前端：**

```bash
cd shopping
npm install
npm run dev
```

看到 `Local: http://localhost:5173/` 就说明前端起来了。

---

## 🌐 打开浏览器访问

| 页面 | 地址 |
| --- | --- |
| 用户端首页 | http://localhost:5173/ |
| 管理端登录 | http://localhost:5173/admin/login |
| 后端 API | http://localhost:8081/api/ |

### 默认账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `admin123` |
| 普通用户 | 去注册页面自己注册 | — |

---

## 📁 项目结构

```
shopping-platform/
├── README.md
├── ecosystem.config.js         PM2 部署配置（可选）
├── .github/workflows/
│   └── deploy-pages.yml        前端自动部署到 GitHub Pages
├── db/
│   ├── schema.sql              建库建表脚本
│   └── data.sql                管理员 + 分类种子数据
├── backend/                    Spring Boot 后端
│   ├── pom.xml
│   ├── fix_cart_table.sql      购物车表重建（出错时用）
│   └── src/main/
│       ├── java/com/example/shopping/
│       │   ├── controller/     接口层
│       │   ├── service/        业务层
│       │   ├── repository/     数据层
│       │   ├── entity/         实体类
│       │   ├── dto/            数据传输对象
│       │   ├── security/       安全认证
│       │   └── config/         配置类
│       └── resources/
│           └── application.yml
└── shopping/                   Vue 3 前端
    ├── package.json
    ├── vite.config.ts
    ├── public/images/          静态商品图（构建时原样拷贝）
    └── src/
        ├── components/         页面组件
        └── router/             路由
```

## 🛠️ 技术栈

- **前端**：Vue 3.5 + TypeScript + Vite 8 + Element Plus 2.13 + Vue Router 5
- **后端**：Spring Boot 3.2.0 + Java 17 + Spring Data JPA + Spring Security + JWT
- **数据库**：MySQL 8.0+

## 📦 打包发布

```bash
# 前端打包（输出到 shopping/dist/）
cd shopping
npm run build

# 需要一个类型检查更严格的构建时用这个（目前项目里还有若干 TS 类型错误，会阻断打包）
npm run build:typecheck

# 后端打 jar 包（输出到 backend/target/shopping-1.0.0.jar）
cd backend
mvn clean package -DskipTests
java -jar target/shopping-1.0.0.jar
```

### 前端部署到子路径

`vite.config.ts` 里的 `base` 由环境变量 `VITE_BASE` 决定，默认 `/`。
部署到 `https://<用户名>.github.io/<仓库名>/` 这类子路径时，构建时传进去即可：

```bash
VITE_BASE=/shopping-platform/ npm run build
```

推送 `main` 分支后，`.github/workflows/deploy-pages.yml` 会自动完成这一步并发布到 GitHub Pages。

## ❓ 常见问题

**Q：启动后端报 `Access denied for user 'root'@'localhost'`？**
A：数据库密码不对。用环境变量 `DB_PASSWORD` 覆盖，或者改 `application.yml` 里的默认值。

**Q：报 `Table 'shopping.xxx' doesn't exist`？**
A：第 1 步的 `db/schema.sql` 没执行成功，重新跑一下。购物车相关表有问题可以再执行 `backend/fix_cart_table.sql`。

**Q：登录后 JWT 报错？**
A：`jwt.secret`（或环境变量 `JWT_SECRET`）太短了，至少 32 个字符。

**Q：商品图片加载不出来？**
A：商品图放在 `shopping/public/images/`，确认文件名和组件里引用的路径一致。

## 🌱 环境准备（第一次装环境看这里）

| 工具 | 最低版本 | 检查命令 |
| --- | --- | --- |
| JDK | 17 | `java -version` |
| Maven | 3.9 | `mvn -v` |
| Node.js | 18 | `node -v` |
| MySQL | 8.0 | `mysql --version` |

Windows 推荐用官网安装包；Mac 推荐 `brew install openjdk@17 maven node mysql`。
