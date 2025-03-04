# User Management System

一个基于Spring Boot的用户管理系统，支持基本的用户管理功能和GitHub OAuth2登录。

## 功能特点

- 用户注册、登录和注销
- 用户资料管理
- 基于JWT的认证
- 基于角色的授权
- GitHub OAuth2集成
- 密码重置功能
- 邮件通知

## 技术栈

- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT
- Thymeleaf
- Bootstrap
- jQuery

## 开始使用

### 前提条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

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
   - 数据库连接信息
   - JWT密钥
   - 邮件服务器配置
   - GitHub OAuth2客户端ID和密钥

### 运行

```bash
./mvnw spring-boot:run
```

应用将在 http://localhost:8080 上运行。

## 安全注意事项

- 确保在生产环境中使用强密码和安全的JWT密钥
- 不要在公共仓库中提交包含敏感信息的配置文件
- 定期更新依赖以修复安全漏洞

## 许可证

[MIT](LICENSE)
