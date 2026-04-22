-- Seed data shared by H2 profile. MySQL loads its copy from init.sql.

INSERT INTO sys_user (id, username, password, nickname, email, status) VALUES
  (1, 'admin', '$2b$10$2U5VvNtPz7r7iedFFaBJOe2JqcuaxYbni2EJ6LOL2uQC2Rq0gAYX.', 'Administrator', 'admin@example.com', 1),
  (2, 'user',  '$2b$10$sNeQqASn5fvdmkWO1UuSr.uAwxmYcckpsc.e1nBhE97IH6o6RHTlu', 'Normal User',   'user@example.com',  1);

INSERT INTO sys_role (id, name, code, remark) VALUES
  (1, '超级管理员', 'ROLE_ADMIN', 'Full access'),
  (2, '普通用户',   'ROLE_USER',  'Read-only demo role');

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1), (2, 2);

INSERT INTO sys_menu (id, parent_id, name, path, component, icon, sort, type, permission) VALUES
  (1,  0, '仪表盘',   '/dashboard',        'dashboard/index',        'Odometer',   1,  1, 'dashboard:view'),
  (10, 0, '系统管理', '/system',           'Layout',                 'Setting',    90, 1, NULL),
  (11, 10,'用户管理', '/system/user',      'system/user/index',      'User',       1,  1, 'system:user:list'),
  (12, 10,'角色管理', '/system/role',      'system/role/index',      'UserFilled', 2,  1, 'system:role:list'),
  (13, 10,'菜单管理', '/system/menu',      'system/menu/index',      'Menu',       3,  1, 'system:menu:list'),
  (20, 0, '内容管理', '/content',          'Layout',                 'Document',   20, 1, NULL),
  (21, 20,'文章管理', '/content/article',  'content/article/index',  'EditPen',    1,  1, 'content:article:list');

INSERT INTO sys_role_menu (role_id, menu_id) VALUES
  (1, 1), (1, 10), (1, 11), (1, 12), (1, 13), (1, 20), (1, 21),
  (2, 1), (2, 20), (2, 21);

INSERT INTO article (title, summary, content, author, status) VALUES
  ('欢迎使用 Admin Demo', '这是一个基于 Spring Boot 3 + Vue 3 的后台管理系统示例。', '## 欢迎\n\n这是示例文章内容。', 'admin', 1),
  ('RBAC 权限模型介绍',   '简单介绍基于角色的访问控制。',                                 '用户 → 角色 → 菜单/权限。', 'admin', 1);
