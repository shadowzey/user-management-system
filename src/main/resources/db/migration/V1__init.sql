-- 初始化数据库表结构
-- 这个文件是为了确保Flyway有一个基线版本

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(20) NOT NULL UNIQUE,
  email VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  phone_number VARCHAR(20),
  real_name VARCHAR(50),
  address VARCHAR(200),
  city VARCHAR(100),
  postal_code VARCHAR(20),
  country VARCHAR(50),
  bio VARCHAR(500),
  enabled BOOLEAN DEFAULT TRUE,
  oauth_id VARCHAR(255),
  oauth_provider VARCHAR(50)
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(20) NOT NULL UNIQUE
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 初始化角色数据
INSERT IGNORE INTO roles(name) VALUES('ROLE_USER');
INSERT IGNORE INTO roles(name) VALUES('ROLE_MODERATOR');
INSERT IGNORE INTO roles(name) VALUES('ROLE_ADMIN');

-- 创建用户日志表
CREATE TABLE IF NOT EXISTS user_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT,
  username VARCHAR(50),
  action VARCHAR(50) NOT NULL,
  ip_address VARCHAR(50),
  user_agent VARCHAR(255),
  status BOOLEAN,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
