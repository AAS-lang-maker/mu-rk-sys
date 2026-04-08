-- 互关关注关系表（互关=好友）
-- 说明：一条记录表示 user_id 关注了 followed_id；互关需要同时存在两条记录。
CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` INT NOT NULL COMMENT '关注者',
  `followed_id` INT NOT NULL COMMENT '被关注者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_followed` (`user_id`, `followed_id`),
  KEY `idx_followed_id` (`followed_id`),
  CONSTRAINT `fk_user_follow_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_follow_followed` FOREIGN KEY (`followed_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';

