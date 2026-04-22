-- MySQL init script: creates schema and seeds demo data.
-- Mounted by docker-compose into /docker-entrypoint-initdb.d/.

CREATE DATABASE IF NOT EXISTS `admin_demo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `admin_demo`;

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `username`   VARCHAR(64)  NOT NULL,
  `password`   VARCHAR(255) NOT NULL,
  `nickname`   VARCHAR(64)  NULL,
  `email`      VARCHAR(128) NULL,
  `avatar`     VARCHAR(255) NULL,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1=enabled,0=disabled',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(64)  NOT NULL,
  `code`       VARCHAR(64)  NOT NULL,
  `remark`     VARCHAR(255) NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id`  BIGINT       NOT NULL DEFAULT 0,
  `name`       VARCHAR(64)  NOT NULL,
  `path`       VARCHAR(128) NULL,
  `component`  VARCHAR(128) NULL,
  `icon`       VARCHAR(64)  NULL,
  `sort`       INT          NOT NULL DEFAULT 0,
  `type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '1=menu,2=button',
  `permission` VARCHAR(128) NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `role_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `article` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(255) NOT NULL,
  `summary`    VARCHAR(500) NULL,
  `content`    LONGTEXT     NULL,
  `author`     VARCHAR(64)  NULL,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1=published,0=draft',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data: admin/admin123 and user/user123.
-- BCrypt hashes generated with strength 10.
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`, `status`) VALUES
  (1, 'admin', '$2b$10$2U5VvNtPz7r7iedFFaBJOe2JqcuaxYbni2EJ6LOL2uQC2Rq0gAYX.', 'Administrator', 'admin@example.com', 1),
  (2, 'user',  '$2b$10$sNeQqASn5fvdmkWO1UuSr.uAwxmYcckpsc.e1nBhE97IH6o6RHTlu', 'Normal User',   'user@example.com',  1)
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);

INSERT INTO `sys_role` (`id`, `name`, `code`, `remark`) VALUES
  (1, '超级管理员', 'ROLE_ADMIN', 'Full access'),
  (2, '普通用户',   'ROLE_USER',  'Read-only demo role')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `icon`, `sort`, `type`, `permission`) VALUES
  (1,  0, '仪表盘',   '/dashboard',        'dashboard/index',        'Odometer', 1,  1, 'dashboard:view'),
  (10, 0, '系统管理', '/system',           'Layout',                 'Setting',  90, 1, NULL),
  (11, 10,'用户管理', '/system/user',      'system/user/index',      'User',     1,  1, 'system:user:list'),
  (12, 10,'角色管理', '/system/role',      'system/role/index',      'UserFilled',2, 1, 'system:role:list'),
  (13, 10,'菜单管理', '/system/menu',      'system/menu/index',      'Menu',     3,  1, 'system:menu:list'),
  (20, 0, '内容管理', '/content',          'Layout',                 'Document', 20, 1, NULL),
  (21, 20,'文章管理', '/content/article',  'content/article/index',  'EditPen',  1,  1, 'content:article:list')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
  (1, 1), (1, 10), (1, 11), (1, 12), (1, 13), (1, 20), (1, 21),
  (2, 1), (2, 20), (2, 21)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

INSERT INTO `article` (`title`, `summary`, `content`, `author`, `status`) VALUES
  ('欢迎使用 Admin Demo', '这是一个基于 Spring Boot 3 + Vue 3 的后台管理系统示例。', '## 欢迎\n\n这是示例文章内容。', 'admin', 1),
  ('RBAC 权限模型介绍',   '简单介绍基于角色的访问控制。',                                 '用户 → 角色 → 菜单/权限。', 'admin', 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);
