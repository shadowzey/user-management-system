-- 添加OAuth2相关字段到users表
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS oauth_id VARCHAR(255) NULL,
ADD COLUMN IF NOT EXISTS oauth_provider VARCHAR(50) NULL;

-- 创建索引以加快OAuth2用户查询
CREATE INDEX IF NOT EXISTS idx_oauth_id ON users (oauth_id);
CREATE INDEX IF NOT EXISTS idx_oauth_provider ON users (oauth_provider);
