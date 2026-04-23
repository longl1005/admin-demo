-- Seed data shared by H2 profile. MySQL loads its copy from init.sql.

INSERT INTO sys_user (id, username, password, nickname, email, status) VALUES
  (1, 'admin', '$2b$10$2U5VvNtPz7r7iedFFaBJOe2JqcuaxYbni2EJ6LOL2uQC2Rq0gAYX.', 'Administrator', 'admin@example.com', 1),
  (2, 'user',  '$2b$10$sNeQqASn5fvdmkWO1UuSr.uAwxmYcckpsc.e1nBhE97IH6o6RHTlu', 'Normal User',   'user@example.com',  1);

INSERT INTO sys_role (id, name, code, remark) VALUES
  (1, '超级管理员', 'ROLE_ADMIN', 'Full access'),
  (2, '普通用户',   'ROLE_USER',  'Read-only demo role');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, sort, type, permission) VALUES
  (1,  0, '仪表盘',     '/dashboard',        'dashboard/index',           'Odometer',   1,  1, 'dashboard:view'),
  (10, 0, '系统管理',   '/system',           'Layout',                    'Setting',    90, 1, NULL),
  (11, 10,'用户管理',   '/system/user',      'system/user/index',         'User',       1,  1, 'system:user:list'),
  (12, 10,'角色管理',   '/system/role',      'system/role/index',         'UserFilled', 2,  1, 'system:role:list'),
  (13, 10,'菜单管理',   '/system/menu',      'system/menu/index',         'Menu',       3,  1, 'system:menu:list'),
  (20, 0, '内容管理',   '/content',          'Layout',                    'Document',   20, 1, NULL),
  (21, 20,'文章管理',   '/content/article',  'content/article/index',     'EditPen',    1,  1, 'content:article:list'),
  (22, 20,'知识分类',   '/content/kb-category', 'content/kb/category/index', 'Folder',  2,  1, 'content:kb:category:list'),
  (23, 20,'知识库',     '/content/kb',       'content/kb/article/index',  'Reading',    3,  1, 'content:kb:article:list');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 1), (1, 10), (1, 11), (1, 12), (1, 13), (1, 20), (1, 21), (1, 22), (1, 23),
  (2, 1), (2, 20), (2, 21), (2, 23);

INSERT INTO article (title, summary, content, author, status) VALUES
  ('欢迎使用 Admin Demo', '这是一个基于 Spring Boot 3 + Vue 3 的后台管理系统示例。', '## 欢迎\n\n这是示例文章内容。', 'admin', 1),
  ('RBAC 权限模型介绍',   '简单介绍基于角色的访问控制。',                                 '用户 → 角色 → 菜单/权限。', 'admin', 1);

INSERT INTO kb_category (id, parent_id, name, description, sort) VALUES
  (1, 0, '快速上手', '新手入门指南', 1),
  (2, 0, '开发指南', '后端 / 前端开发相关文档', 2),
  (3, 2, '后端',     'Spring Boot 相关', 1),
  (4, 2, '前端',     'Vue 3 + Element Plus 相关', 2),
  (5, 0, '常见问题', 'FAQ 与故障排查', 3);

INSERT INTO kb_article (category_id, title, summary, content, tags, author, status) VALUES
  (1, '项目简介', 'Admin Demo 项目概览', '# Admin Demo\n\nSpring Boot 3 + Vue 3 的后台管理示例，包含 RBAC、JWT、CRUD 与知识库。', '入门,概览', 'admin', 1),
  (1, '本地运行', '如何在本地启动前后端', '## 启动\n\n1. `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=h2`\n2. `cd frontend && pnpm install && pnpm dev`\n\n打开 http://localhost:5173 即可。', '入门,运行', 'admin', 1),
  (3, 'JWT 鉴权流程', '登录、颁发、校验', '## 流程\n\n1. `/auth/login` 返回 token\n2. 前端存 `localStorage.token`\n3. 每次请求带 `Authorization: Bearer <token>`\n4. `JwtAuthenticationFilter` 校验并填充 SecurityContext', '后端,JWT,安全', 'admin', 1),
  (4, 'Element Plus 约定', '组件使用规范', '## 规范\n\n- 使用 `<el-card>` 作为页面容器\n- 表单用 `<el-form>` + rules 校验\n- 弹窗使用 `<el-dialog>` + `v-model`', '前端,ElementPlus', 'admin', 1),
  (5, '启动报错排查', '常见错误与对应方案', '## 常见错误\n\n- **端口占用**：8080 / 5173 已被使用，修改 `application.yml` 或 `vite.config.ts`。\n- **BCrypt 不匹配**：重新用项目的密码哈希；不要自己生成。', 'FAQ,排查', 'admin', 1);
