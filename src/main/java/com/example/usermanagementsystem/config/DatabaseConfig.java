package com.example.usermanagementsystem.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 数据库配置类
 * 用于解决Flyway和JPA之间的循环依赖问题
 */
@Configuration
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    /**
     * 创建JdbcTemplate
     */
    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 创建事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    /**
     * 手动执行必要的数据库更新
     * 这是为了确保OAuth2相关的字段存在于users表中
     */
    @Bean
    public Boolean ensureOAuth2FieldsExist() {
        try {
            JdbcTemplate jdbc = jdbcTemplate();
            
            // 检查oauth_id列是否存在
            boolean oauthIdExists = columnExists(jdbc, "users", "oauth_id");
            if (!oauthIdExists) {
                jdbc.execute("ALTER TABLE users ADD COLUMN oauth_id VARCHAR(255) NULL");
            }
            
            // 检查oauth_provider列是否存在
            boolean oauthProviderExists = columnExists(jdbc, "users", "oauth_provider");
            if (!oauthProviderExists) {
                jdbc.execute("ALTER TABLE users ADD COLUMN oauth_provider VARCHAR(50) NULL");
            }
            
            // 创建索引
            try {
                jdbc.execute("CREATE INDEX idx_oauth_id ON users (oauth_id)");
            } catch (Exception e) {
                // 索引可能已存在，忽略错误
            }
            
            try {
                jdbc.execute("CREATE INDEX idx_oauth_provider ON users (oauth_provider)");
            } catch (Exception e) {
                // 索引可能已存在，忽略错误
            }
            
            return true; // 表示操作成功
            
        } catch (Exception e) {
            // 记录错误但不阻止应用启动
            e.printStackTrace();
            return false; // 表示操作失败
        }
    }
    
    /**
     * 检查列是否存在
     */
    private boolean columnExists(JdbcTemplate jdbc, String tableName, String columnName) {
        try {
            String query = "SELECT COUNT(*) FROM information_schema.columns " +
                           "WHERE table_name = ? AND column_name = ? AND table_schema = DATABASE()";
            Integer count = jdbc.queryForObject(query, Integer.class, tableName, columnName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
