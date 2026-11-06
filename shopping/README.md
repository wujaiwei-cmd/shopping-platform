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