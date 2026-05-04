-- 创建 user_tags 表
USE music_db;

CREATE TABLE User_Tags (
    UT_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id INT NOT NULL COMMENT '打标签的用户ID',
    tag_id INT NOT NULL COMMENT '关联字典表的ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打标签时间',
    status INT DEFAULT 0 COMMENT '标签状态 (0正常，1审核中，2屏蔽)',
    use_count INT DEFAULT 1,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户标签表';
