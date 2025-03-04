# GitHub OAuth2 登录集成指南

本文档提供了如何在用户管理系统中配置和使用GitHub OAuth2登录功能的详细说明。

## 配置步骤

### 1. 在GitHub创建OAuth应用

1. 登录到您的GitHub账户
2. 访问 Settings > Developer settings > OAuth Apps
3. 点击 "New OAuth App" 按钮
4. 填写应用信息：
   - Application name: 用户管理系统
   - Homepage URL: http://localhost:8080
   - Authorization callback URL: http://localhost:8080/login/oauth2/code/github
5. 点击 "Register application" 按钮
6. 创建完成后，您将获得Client ID和Client Secret

### 2. 配置应用程序

在`application.properties`文件中，使用您从GitHub获取的Client ID和Client Secret替换以下配置：

```properties
# GitHub OAuth2 Configuration
spring.security.oauth2.client.registration.github.client-id=your-github-client-id
spring.security.oauth2.client.registration.github.client-secret=your-github-client-secret
spring.security.oauth2.client.registration.github.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}
spring.security.oauth2.client.registration.github.scope=user:email,read:user
```

### 3. 启用Flyway数据库迁移

确保Flyway配置已启用，以便创建必要的数据库表结构：

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

## 使用方法

1. 启动应用程序
2. 访问登录页面：http://localhost:8080/login
3. 点击 "使用GitHub登录" 按钮
4. 您将被重定向到GitHub授权页面
5. 授权后，您将被重定向回应用程序并自动登录

## 技术实现说明

本系统的GitHub OAuth2登录功能通过以下组件实现：

1. **CustomOAuth2UserService**: 处理OAuth2用户信息，将GitHub用户信息转换为系统用户
2. **OAuth2AuthenticationSuccessHandler**: 处理OAuth2认证成功后的逻辑，生成JWT令牌
3. **User实体类扩展**: 添加了`oauthId`和`oauthProvider`字段，用于存储OAuth2用户信息
4. **数据库迁移**: 使用Flyway创建必要的数据库表结构

## 故障排除

如果遇到以下问题，请尝试相应的解决方案：

1. **无法连接到GitHub**: 检查网络连接和GitHub服务状态
2. **认证失败**: 确认Client ID和Client Secret配置正确
3. **数据库错误**: 确保数据库连接正常，并且Flyway迁移脚本已正确执行

## 安全注意事项

1. 不要在代码中硬编码Client Secret
2. 定期检查和更新OAuth2配置
3. 监控异常登录活动

如需更多帮助，请联系系统管理员。
