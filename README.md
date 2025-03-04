# 用户管理系统

一个基于Spring Boot的用户管理系统，支持基本的用户管理功能和GitHub OAuth2登录，实现了前后端分离的架构设计。

## 功能特点

- 用户注册、登录和注销
- 用户资料管理与更新
- 基于JWT的认证（Cookie存储）
- 基于角色的授权（ROLE_USER, ROLE_ADMIN）
- GitHub OAuth2第三方登录集成
- 密码重置功能
- 邮件通知服务
- 用户头像上传与管理

## 技术栈

- 后端：
  - Spring Boot 3.x
  - Spring Security 6.x（新配置方式）
  - Spring Data JPA
  - MySQL 8.0+
  - JWT认证
  - SLF4J日志系统

- 前端：
  - Thymeleaf模板引擎
  - Bootstrap 5
  - jQuery
  - 原生JavaScript

## 开始使用

### 前提条件

- JDK 17+
- Gradle 8.0+
- MySQL 8.0+
- GitHub账号（用于OAuth2配置）

### 配置

1. 克隆仓库
```bash
git clone https://github.com/yourusername/user-management-system.git
cd user-management-system
```

2. 配置数据库和应用属性
```bash
cp src/main/resources/application-template.properties src/main/resources/application.properties
```

3. 编辑`application.properties`文件，填入您的配置信息：
   - 数据库连接信息（URL、用户名和密码）
   - JWT密钥（建议使用强随机密钥）
   - 邮件服务器配置（SMTP服务器、端口、账号和密码）
   - GitHub OAuth2客户端ID和密钥（需在GitHub开发者设置中创建）

4. 创建MySQL数据库
```sql
CREATE DATABASE mydatabase CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 运行

```bash
./gradlew bootRun
```

应用将在 http://localhost:8080 上运行。

### Docker部署

项目包含了Docker配置，可以使用Docker Compose进行部署：

```bash
# 构建和启动容器
$ docker-compose up -d

# 查看容器状态
$ docker-compose ps

# 查看应用日志
$ docker-compose logs -f app

# 停止并移除容器
$ docker-compose down
```

注意：使用Docker部署前，请先在compose.yaml文件中替换所有敏感信息。

### 默认账户

系统将自动创建一个管理员账户：
- 用户名：admin
- 密码：在application.properties中配置（app.admin.password）

## 安全注意事项

- 确保在生产环境中使用强密码和安全的JWT密钥
- 不要在公共仓库中提交包含敏感信息的配置文件
- 定期更新依赖以修复安全漏洞
- 使用HTTPS协议保护生产环境中的应用
- OAuth2回调URL必须与GitHub应用设置中的重定向URL匹配

## 主要功能说明

### 用户认证

系统支持两种认证方式：
1. 用户名/密码登录：使用JWT令牌存储在Cookie中
2. GitHub OAuth2登录：通过GitHub账号直接登录，自动创建对应的本地账户

### 用户角色

系统定义了两种角色：
- ROLE_USER：普通用户，可以管理自己的账户信息
- ROLE_ADMIN：管理员，可以管理所有用户和系统设置

## 许可证

[MIT](LICENSE)
