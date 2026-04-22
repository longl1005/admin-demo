# Admin Demo

基于 **Spring Boot 3** + **Vue 3** 的后台管理系统脚手架，开箱即用，包含登录、JWT 鉴权、RBAC 用户/角色/菜单管理和一个示例 CRUD（文章管理）。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.2 · Java 17 · Spring Security · MyBatis-Plus 3.5 · JJWT 0.12 · MySQL 8 / H2 |
| 前端 | Vue 3 · Vite 5 · TypeScript · Element Plus · Pinia · Vue Router 4 · Axios |
| 基建 | Docker Compose（MySQL）· Maven · pnpm |

## 目录结构

```
admin-demo/
├── backend/                      # Spring Boot 后端
│   ├── src/main/java/com/example/admin/
│   │   ├── AdminApplication.java
│   │   ├── config/               # Security / MyBatis-Plus / CORS
│   │   ├── security/             # JwtUtil / JwtAuthenticationFilter / UserDetailsService
│   │   ├── controller/           # Auth / User / Role / Menu / Article
│   │   ├── service/              # 服务层
│   │   ├── mapper/               # MyBatis-Plus Mapper
│   │   ├── entity/               # 数据库实体
│   │   ├── dto/                  # 请求 / 响应 DTO
│   │   └── common/               # Result / 全局异常处理
│   ├── src/main/resources/
│   │   ├── application.yml       # 通用配置（JWT、CORS、MyBatis-Plus）
│   │   ├── application-dev.yml   # MySQL 数据源（默认激活 dev）
│   │   ├── application-h2.yml    # H2 内存数据源（无需 MySQL 即可运行）
│   │   └── db/                   # init.sql（MySQL）+ schema-h2.sql / data.sql
│   └── pom.xml
├── frontend/                     # Vue 3 前端
│   ├── src/
│   │   ├── api/                  # 接口封装（axios）
│   │   ├── stores/               # Pinia（auth）
│   │   ├── router/               # 路由 + 守卫
│   │   ├── layouts/              # DefaultLayout（菜单 / 头部）
│   │   ├── views/                # login / dashboard / system / content
│   │   └── main.ts
│   ├── vite.config.ts
│   └── package.json
├── docker-compose.yml            # MySQL 8（带自动初始化 init.sql）
└── README.md
```

## 功能

- 🔐 登录 / 登出（Spring Security + JWT，无状态）
- 👤 用户管理（分页、增删改、分配角色）
- 🏷️ 角色管理（分页、增删改）
- 📂 菜单管理（树形只读展示，按角色过滤）
- 📝 文章管理（示例 CRUD）
- 🎯 基于角色 / 权限标识的前端与后端双重鉴权
- 🧭 Pinia store + 路由守卫
- 🌐 CORS + axios 拦截器统一处理 401 / 错误提示

## 快速开始

### 方式 A：H2 内存库（无需 MySQL，推荐本地快速体验）

```bash
# 后端（端口 8080）
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
# 或使用系统 maven：
# mvn spring-boot:run -Dspring-boot.run.profiles=h2

# 前端（端口 5173）
cd frontend
pnpm install
pnpm dev
```

打开 http://localhost:5173，使用默认账号登录：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `admin` | `admin123` | `ROLE_ADMIN`（可看全部菜单、可增删改） |
| `user` | `user123` | `ROLE_USER`（只读，仅"仪表盘 / 文章管理"） |

### 方式 B：MySQL（默认 dev profile）

```bash
# 1) 启动 MySQL（自动执行 backend/src/main/resources/db/init.sql 建表 + 造数据）
docker compose up -d db

# 2) 后端
cd backend
./mvnw spring-boot:run

# 3) 前端
cd frontend
pnpm install
pnpm dev
```

MySQL 连接信息（见 `docker-compose.yml`）：

- host: `localhost:3306`
- database: `admin_demo`
- user / pwd: `admin` / `admin123`
- root pwd: `root`

## 构建

```bash
# 后端 jar 包
cd backend && ./mvnw clean package -DskipTests
# 产物：backend/target/admin-demo-backend-0.0.1-SNAPSHOT.jar

# 前端 dist
cd frontend && pnpm build
# 产物：frontend/dist/
```

## API 一览

所有接口均以 `/api` 为前缀。

| 方法 | 路径 | 鉴权 | 说明 |
| --- | --- | --- | --- |
| POST | `/auth/login` | 公开 | 用户名/密码登录，返回 JWT |
| POST | `/auth/logout` | 公开 | 客户端删除 token 即可（无状态） |
| GET | `/auth/me` | 已登录 | 当前用户信息 + 角色 + 权限 |
| GET | `/auth/menus` | 已登录 | 当前用户可见的菜单树 |
| GET | `/system/users` | ADMIN | 用户分页 |
| POST / PUT / DELETE | `/system/users/{id}` | ADMIN | 用户增改删 |
| GET | `/system/users/{id}/roles` | ADMIN | 用户已分配的角色 id 列表 |
| GET | `/system/roles` / `/all` | ADMIN | 角色分页 / 全量 |
| POST / PUT / DELETE | `/system/roles/{id}` | ADMIN | 角色增改删 |
| GET | `/system/menus/tree` | ADMIN | 全量菜单树 |
| GET | `/content/articles` | 已登录 | 文章分页 |
| GET | `/content/articles/{id}` | 已登录 | 文章详情 |
| POST / PUT / DELETE | `/content/articles/{id}` | ADMIN | 文章增改删 |

## 安全

- JWT secret 通过 `APP_JWT_SECRET` 环境变量覆盖；默认值仅用于本地开发，**生产环境必须替换**。
- 密码使用 BCrypt（Spring Security 默认 cost=10）。
- 所有接口默认要求认证；通过方法上 `@PreAuthorize("hasRole('ADMIN')")` 做进一步授权。
- CORS 白名单在 `application.yml` 的 `app.cors.allowed-origins` 配置，默认仅允许 `http://localhost:5173` / `http://127.0.0.1:5173`。

## 开发提示

- 前端开发服务器已配置代理：`/api` → `http://localhost:8080`。
- 后端使用 `context-path: /api`，因此接口实际路径为 `http://localhost:8080/api/...`。
- MyBatis-Plus 开启了逻辑删除（`deleted` 字段）与分页插件。
- 组件 & 图标按需自动引入（`unplugin-auto-import` + `unplugin-vue-components`）。

## License

MIT
