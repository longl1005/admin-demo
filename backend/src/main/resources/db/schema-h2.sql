-- H2 schema (MySQL compatibility mode). Used for local testing without MySQL.

DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS article;

CREATE TABLE sys_user (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  username   VARCHAR(64)  NOT NULL UNIQUE,
  password   VARCHAR(255) NOT NULL,
  nickname   VARCHAR(64),
  email      VARCHAR(128),
  avatar     VARCHAR(255),
  status     TINYINT      NOT NULL DEFAULT 1,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE sys_role (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(64)  NOT NULL,
  code       VARCHAR(64)  NOT NULL UNIQUE,
  remark     VARCHAR(255),
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_role (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sys_menu (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  parent_id  BIGINT       NOT NULL DEFAULT 0,
  name       VARCHAR(64)  NOT NULL,
  path       VARCHAR(128),
  component  VARCHAR(128),
  icon       VARCHAR(64),
  sort       INT          NOT NULL DEFAULT 0,
  type       TINYINT      NOT NULL DEFAULT 1,
  permission VARCHAR(128),
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0
);

CREATE TABLE sys_role_menu (
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE article (
  id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
  title      VARCHAR(255) NOT NULL,
  summary    VARCHAR(500),
  content    CLOB,
  author     VARCHAR(64),
  status     TINYINT      NOT NULL DEFAULT 1,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted    TINYINT      NOT NULL DEFAULT 0
);
