-- ============================================
-- 修复缺失的数据表 - 完整版
-- 执行此脚本前请确保已连接到 music_db 数据库
-- ============================================

USE music_db;

-- 1. 检查并创建 tags_dictionary 表（标签字典表）
CREATE TABLE IF NOT EXISTS Tags_Dictionary (
    tag_id INT PRIMARY KEY AUTO_INCREMENT,
    tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名',
    use_count INT DEFAULT 0 COMMENT '全站使用该标签的总热度',
    INDEX idx_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签字典表';

-- 2. 检查并创建 user_tags 表（用户标签表）
CREATE TABLE IF NOT EXISTS User_Tags (
    UT_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '打标签的用户ID',
    tag_id INT NOT NULL COMMENT '关联字典表的ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打标签时间',
    status INT DEFAULT 0 COMMENT '标签状态 (0正常，1审核中，2屏蔽)',
    use_count INT DEFAULT 1,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签表';

-- 3. 插入一些测试标签数据（可选）
INSERT IGNORE INTO Tags_Dictionary (tag_name, use_count) VALUES 
('流行', 100),
('摇滚', 80),
('民谣', 60),
('电子', 70),
('说唱', 90),
('经典', 85),
('抒情', 75);

-- 4. 验证表是否创建成功
SELECT '========== 验证表创建结果 ==========' as info;
SHOW TABLES LIKE 'tags_dictionary';
SHOW TABLES LIKE 'user_tags';

SELECT '========== 标签数据 ==========' as info;
SELECT * FROM Tags_Dictionary;

SELECT '✅ 修复完成！现在可以重启应用了！' as result;
