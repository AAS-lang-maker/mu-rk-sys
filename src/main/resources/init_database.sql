CREATE DATABASE IF NOT EXISTS music_db;
USE music_db;

CREATE TABLE `user` (
                        `user_id` int NOT NULL AUTO_INCREMENT,
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `user_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        `password` varchar(100) NOT NULL COMMENT '密码',
                        PRIMARY KEY (`user_id`),
                        KEY `idx_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
CREATE TABLE `category` (
                            `category_id` int NOT NULL AUTO_INCREMENT COMMENT '分类id',
                            `categoty_name` varchar(50) NOT NULL COMMENT '分类名称',
                            `category_status` tinyint DEFAULT '1' COMMENT '状态id(1为启用，0为禁用)',
                            `category_sort` int DEFAULT NULL,
                            PRIMARY KEY (`category_id`),
                            UNIQUE KEY `categoty_name` (`categoty_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='榜单分类表';
CREATE TABLE `personal_rank` (
                                 `rank_id` int NOT NULL AUTO_INCREMENT COMMENT '个人榜单id',
                                 `rank_name` varchar(50) NOT NULL COMMENT '榜单名',
                                 `user_id` int NOT NULL COMMENT '关联用户id',
                                 `category_id` int NOT NULL COMMENT '分类id',
                                 `vote_count` int DEFAULT '0' COMMENT '总支持数',
                                 `publish_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '个人榜单发布时间',
                                 `target_id` int NOT NULL COMMENT '分类下的具体歌手或作品id',
                                 `love_count` int DEFAULT '0' COMMENT '收藏总数',
                                 PRIMARY KEY (`rank_id`),
                                 KEY `category_id` (`category_id`),
                                 KEY `user_id` (`user_id`),
                                 CONSTRAINT `personal_rank_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`),
                                 CONSTRAINT `personal_rank_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='个人榜单表';
CREATE TABLE `singer` (
                          `singer_id` int NOT NULL AUTO_INCREMENT COMMENT '歌手id',
                          `singer_name` varchar(100) NOT NULL COMMENT '歌手名',
                          `singer_img` varchar(255) DEFAULT NULL COMMENT '歌手图片',
                          `category_id` int NOT NULL COMMENT '分类id',
                          `singer_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '歌手创建时间',
                          `singer_status` tinyint DEFAULT '1' COMMENT '状态id(1为启用，0为禁用',
                          `singer_sort` int DEFAULT '0' COMMENT '歌手前端展示排序',
                          PRIMARY KEY (`singer_id`),
                          KEY `category_id` (`category_id`),
                          CONSTRAINT `singer_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='歌手信息表';
CREATE TABLE `work` (
                        `work_id` int NOT NULL AUTO_INCREMENT COMMENT '影视及游戏作品id',
                        `work_name` varchar(100) NOT NULL COMMENT '影视及游戏作品名',
                        `work_img` varchar(255) DEFAULT NULL COMMENT '相关图片',
                        `category_id` int NOT NULL COMMENT '分类id',
                        `work_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '作品创建时间',
                        `work_status` tinyint DEFAULT '1' COMMENT '状态id(1为启用，0为禁用',
                        `work_sort` int DEFAULT '0' COMMENT '影视及游戏作品前端展示排序',
                        PRIMARY KEY (`work_id`),
                        KEY `category_id` (`category_id`),
                        CONSTRAINT `work_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='影视及游戏信息表';
CREATE TABLE `song` (
                        `song_id` int NOT NULL AUTO_INCREMENT COMMENT '歌曲id',
                        `song_name` varchar(50) NOT NULL COMMENT '歌曲名',
                        `song_img` varchar(255) DEFAULT NULL COMMENT '相关图片',
                        `song_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '歌曲添加时间',
                        `song_status` tinyint DEFAULT '1' COMMENT '状态id(1为启用，0为禁用)',
                        `singer_id` int DEFAULT NULL COMMENT '关联歌手表',
                        `work_id` int DEFAULT NULL COMMENT '关联作品表',
                        `duration` int DEFAULT '0' COMMENT '歌曲播放时长',
                        `song_url` varchar(250) DEFAULT NULL COMMENT 'mp3路径',
                        PRIMARY KEY (`song_id`),
                        KEY `singer_id` (`singer_id`),
                        KEY `work_id` (`work_id`),
                        CONSTRAINT `song_ibfk_1` FOREIGN KEY (`singer_id`) REFERENCES `singer` (`singer_id`),
                        CONSTRAINT `song_ibfk_2` FOREIGN KEY (`work_id`) REFERENCES `work` (`work_id`),
                        CONSTRAINT `song_chk_1` CHECK ((((`singer_id` is not null) and (`work_id` is null)) or ((`work_id` is not null) and (`singer_id` is null))))
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='总歌曲表';
CREATE TABLE `rank_song` (
                             `rs_id` int NOT NULL AUTO_INCREMENT COMMENT '榜单歌曲id主键',
                             `rank_id` int NOT NULL COMMENT '关联个人榜单表',
                             `song_id` int NOT NULL COMMENT '关联总歌曲表',
                             `ranking` int NOT NULL COMMENT '歌曲排名',
                             `rank_create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `rank_update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             PRIMARY KEY (`rs_id`),
                             UNIQUE KEY `ur_song` (`rank_id`,`song_id`),
                             KEY `fk_rank_song_song_id` (`song_id`),
                             CONSTRAINT `fk_rank_song_rank_id` FOREIGN KEY (`rank_id`) REFERENCES `personal_rank` (`rank_id`) ON DELETE CASCADE,
                             CONSTRAINT `fk_rank_song_song_id` FOREIGN KEY (`song_id`) REFERENCES `song` (`song_id`)
) ENGINE=InnoDB AUTO_INCREMENT=166 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='榜单歌曲排名表';

CREATE TABLE `comment` (
                           `com_id` int NOT NULL AUTO_INCREMENT COMMENT '评论区主键',
                           `rank_id` int NOT NULL COMMENT '关联个人榜单',
                           `user_id` int DEFAULT NULL COMMENT '关联用户id',
                           `parent_id` int DEFAULT NULL COMMENT '父评论id',
                           `comment_content` varchar(50) DEFAULT NULL COMMENT '评论内容',
                           `id_delete` tinyint DEFAULT '0' COMMENT '该评论状态(1表示已删除，0表示未删除)',
                           `comment_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评论发布时间',
                           PRIMARY KEY (`com_id`),
                           KEY `parent_id` (`parent_id`),
                           KEY `idx_rank_id` (`rank_id`),
                           KEY `idx_user_id` (`user_id`),
                           KEY `idx_com_time` (`comment_time`),
                           CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`rank_id`) REFERENCES `personal_rank` (`rank_id`) ON DELETE CASCADE,
                           CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE,
                           CONSTRAINT `comment_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `comment` (`com_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='榜单评论表';
CREATE TABLE `love_record` (
                               `love_id` int NOT NULL AUTO_INCREMENT COMMENT '投票记录id',
                               `user_id` int DEFAULT NULL COMMENT '用户id',
                               `ip` varchar(255) DEFAULT NULL,
                               `rank_id` int NOT NULL COMMENT '关联个人榜单',
                               `love_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
                               PRIMARY KEY (`love_id`),
                               UNIQUE KEY `lp_ip` (`rank_id`,`ip`),
                               KEY `user_id` (`user_id`),
                               CONSTRAINT `fk_love_record_rank_id` FOREIGN KEY (`rank_id`) REFERENCES `personal_rank` (`rank_id`) ON DELETE CASCADE,
                               CONSTRAINT `love_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏记录表';
CREATE TABLE `like_comment` (
                                `like_id` int NOT NULL AUTO_INCREMENT COMMENT '主键点赞id',
                                `com_id` int NOT NULL COMMENT '评论id',
                                `user_id` int DEFAULT NULL COMMENT '用户',
                                PRIMARY KEY (`like_id`),
                                UNIQUE KEY `lc_id` (`com_id`,`user_id`),
                                KEY `idx_comment_id` (`com_id`),
                                KEY `idx_user_id` (`user_id`),
                                CONSTRAINT `like_comment_ibfk_1` FOREIGN KEY (`com_id`) REFERENCES `comment` (`com_id`) ON DELETE CASCADE,
                                CONSTRAINT `like_comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论点赞表';
CREATE TABLE `vote_record` (
                               `vote_id` int NOT NULL AUTO_INCREMENT COMMENT '投票记录id',
                               `user_id` int DEFAULT NULL COMMENT '用户id',
                               `ip` varchar(255) DEFAULT NULL,
                               `rank_id` int NOT NULL COMMENT '关联个人榜单',
                               `vote_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
                               PRIMARY KEY (`vote_id`),
                               UNIQUE KEY `vp_ip` (`rank_id`,`ip`),
                               KEY `user_id` (`user_id`),
                               CONSTRAINT `fk_vote_record_rank_id` FOREIGN KEY (`rank_id`) REFERENCES `personal_rank` (`rank_id`) ON DELETE CASCADE,
                               CONSTRAINT `vote_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='投票记录表';
INSERT INTO music_db.category (category_id, categoty_name, category_status, category_sort) VALUES (1, '歌手明星', 1, 1);
INSERT INTO music_db.category (category_id, categoty_name, category_status, category_sort) VALUES (2, '影视游戏', 1, 2);
set foreign_key_checks =0;
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (8, 12, 12, null, '你好', 1, '2026-02-14 23:49:55');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (9, 12, 16, null, '你好哦', 0, '2026-02-15 15:15:47');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (10, 14, 17, null, '我的', 0, '2026-02-15 22:09:18');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (11, 14, 17, null, '我的', 0, '2026-02-15 22:09:20');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (12, 14, 17, null, '我的', 1, '2026-02-15 22:09:22');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (13, 13, 17, null, '娃娃', 0, '2026-02-15 22:09:27');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (14, 12, 17, null, '我的', 0, '2026-02-15 22:11:19');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (15, 12, 17, null, '我的', 0, '2026-02-15 22:11:21');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (16, 14, 17, null, '？', 0, '2026-02-15 22:11:46');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (17, 12, 17, null, '？？', 1, '2026-02-15 22:24:15');
INSERT INTO music_db.comment (com_id, rank_id, user_id, parent_id, comment_content, id_delete, comment_time) VALUES (18, 12, 17, null, '？', 0, '2026-02-16 00:18:52');
INSERT INTO music_db.like_comment (like_id, com_id, user_id) VALUES (6, 8, 12);
INSERT INTO music_db.like_comment (like_id, com_id, user_id) VALUES (11, 15, 12);
INSERT INTO music_db.love_record (love_id, user_id, ip, rank_id, love_time) VALUES (2, 12, '127.0.0.1', 13, '2026-02-09 16:54:57');
INSERT INTO music_db.love_record (love_id, user_id, ip, rank_id, love_time) VALUES (3, 12, '127.0.0.1', 14, '2026-02-09 16:55:03');
INSERT INTO music_db.love_record (love_id, user_id, ip, rank_id, love_time) VALUES (21, 12, null, 14, '2026-03-02 17:36:01');
INSERT INTO music_db.personal_rank (rank_id, rank_name, user_id, category_id, vote_count, publish_time, target_id, love_count) VALUES (12, '我的最爱123', 12, 1, 1, '2026-03-08 15:03:07', 0, 0);
INSERT INTO music_db.personal_rank (rank_id, rank_name, user_id, category_id, vote_count, publish_time, target_id, love_count) VALUES (13, '不是我的了666', 12, 1, 0, '2026-02-10 21:05:52', 0, 0);
INSERT INTO music_db.personal_rank (rank_id, rank_name, user_id, category_id, vote_count, publish_time, target_id, love_count) VALUES (14, '好的666', 12, 1, 1, '2026-03-08 16:51:49', 0, 0);
INSERT INTO music_db.personal_rank (rank_id, rank_name, user_id, category_id, vote_count, publish_time, target_id, love_count) VALUES (16, '我的歌单', 12, 1, 0, '2026-03-08 16:51:33', 0, 0);
INSERT INTO music_db.personal_rank (rank_id, rank_name, user_id, category_id, vote_count, publish_time, target_id, love_count) VALUES (17, '我的歌单（泰勒版）', 12, 1, 0, '2026-03-06 21:49:43', 0, 0);
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (130, 13, 1, 1, '2026-02-10 21:05:52', '2026-02-10 21:05:52');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (131, 13, 10, 2, '2026-02-10 21:05:52', '2026-02-10 21:05:52');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (132, 13, 11, 3, '2026-02-10 21:05:52', '2026-02-10 21:05:52');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (133, 13, 14, 4, '2026-02-10 21:05:52', '2026-02-10 21:05:52');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (138, 17, 11, 1, '2026-03-06 21:49:43', '2026-03-06 21:49:43');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (139, 17, 12, 2, '2026-03-06 21:49:43', '2026-03-06 21:49:43');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (140, 17, 10, 3, '2026-03-06 21:49:43', '2026-03-06 21:49:43');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (141, 17, 7, 4, '2026-03-06 21:49:43', '2026-03-06 21:49:43');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (142, 12, 12, 1, '2026-03-08 15:03:07', '2026-03-08 15:03:07');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (143, 12, 10, 2, '2026-03-08 15:03:07', '2026-03-08 15:03:07');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (144, 12, 11, 3, '2026-03-08 15:03:07', '2026-03-08 15:03:07');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (145, 12, 13, 4, '2026-03-08 15:03:07', '2026-03-08 15:03:07');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (158, 16, 7, 1, '2026-03-08 16:51:33', '2026-03-08 16:51:33');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (159, 16, 10, 2, '2026-03-08 16:51:33', '2026-03-08 16:51:33');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (160, 16, 11, 3, '2026-03-08 16:51:33', '2026-03-08 16:51:33');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (161, 16, 12, 4, '2026-03-08 16:51:33', '2026-03-08 16:51:33');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (162, 14, 6, 1, '2026-03-08 16:51:49', '2026-03-08 16:51:49');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (163, 14, 7, 2, '2026-03-08 16:51:49', '2026-03-08 16:51:49');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (164, 14, 12, 3, '2026-03-08 16:51:49', '2026-03-08 16:51:49');
INSERT INTO music_db.rank_song (rs_id, rank_id, song_id, ranking, rank_create_time, rank_update_time) VALUES (165, 14, 11, 4, '2026-03-08 16:51:49', '2026-03-08 16:51:49');
INSERT INTO music_db.singer (singer_id, singer_name, singer_img, category_id, singer_create_time, singer_status, singer_sort) VALUES (1, '周杰伦', '', 1, '2026-02-02 11:53:19', 1, 1);
INSERT INTO music_db.singer (singer_id, singer_name, singer_img, category_id, singer_create_time, singer_status, singer_sort) VALUES (2, '陈奕迅', '', 1, '2026-02-02 11:53:19', 1, 2);
INSERT INTO music_db.singer (singer_id, singer_name, singer_img, category_id, singer_create_time, singer_status, singer_sort) VALUES (3, '林俊杰', '', 1, '2026-02-02 11:53:19', 1, 3);
INSERT INTO music_db.singer (singer_id, singer_name, singer_img, category_id, singer_create_time, singer_status, singer_sort) VALUES (4, 'Taylor Swift', '', 1, '2026-02-02 11:53:19', 1, 4);
INSERT INTO music_db.singer (singer_id, singer_name, singer_img, category_id, singer_create_time, singer_status, singer_sort) VALUES (5, '邓紫棋', '', 1, '2026-02-02 11:53:19', 1, 5);
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (1, ' 晴天 ', null, '2003-07-31 00:00:00', 1, 1, null, 269, '周杰伦-,A-LNK - 晴天.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (2, ' 七里香 ', null, '2004-08-03 00:00:00', 1, 1, null, 299, 'Xai小爱 - 七里香.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (3, ' 青花瓷 ', null, '2007-11-02 00:00:00', 1, 1, null, 239, '周杰伦,-IN-K - 青花瓷.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (4, ' 浮夸 ', null, '2005-06-07 00:00:00', 1, 2, null, 284, '陈奕迅 - 浮夸.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (5, 'K 歌之王 ', null, '2000-09-29 00:00:00', 1, 2, null, 218, '陈奕迅 - K歌之王.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (6, ' 十年 ', null, '2003-04-01 00:00:00', 1, 2, null, 204, '陈奕迅 - 十年.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (7, ' 江南 ', null, '2004-06-04 00:00:00', 1, 3, null, 260, '林俊杰 - 江南.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (8, ' 不为谁而作的歌 ', null, '2015-12-25 00:00:00', 1, 3, null, 296, '林俊杰 - 不为谁而作的歌.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (9, ' 小酒窝 ', null, '2008-10-18 00:00:00', 1, 3, null, 221, '林俊杰 - 小酒窝.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (10, 'Love Story', null, '2008-09-12 00:00:00', 1, 4, null, 234, 'Taylor Swift - Love Story.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (11, 'Shake It Off', null, '2014-08-18 00:00:00', 1, 4, null, 239, 'Taylor Swift - Shake It Off (Taylor\'s Version).mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (12, 'Blank Space', null, '2014-11-10 00:00:00', 1, 4, null, 231, 'Taylor Swift - Blank Space.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (13, ' 泡沫 ', null, '2012-07-05 00:00:00', 1, 5, null, 251, 'G.E.M.邓紫棋 - 泡沫 (G.E.M.重生版).mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (14, ' 光年之外 ', null, '2016-12-30 00:00:00', 1, 5, null, 239, 'G.E.M.邓紫棋 - 光年之外.mp3');
INSERT INTO music_db.song (song_id, song_name, song_img, song_create_time, song_status, singer_id, work_id, duration, song_url) VALUES (15, ' 来自天堂的魔鬼 ', null, '2015-11-06 00:00:00', 1, 5, null, 240, 'G.E.M.邓紫棋 - 来自天堂的魔鬼.mp3');
INSERT INTO music_db.vote_record (vote_id, user_id, ip, rank_id, vote_time) VALUES (30, 17, null, 14, '2026-02-16 00:16:23');
INSERT INTO music_db.vote_record (vote_id, user_id, ip, rank_id, vote_time) VALUES (40, 17, null, 12, '2026-02-16 00:27:04');
INSERT INTO music_db.vote_record (vote_id, user_id, ip, rank_id, vote_time) VALUES (117, 12, null, 14, '2026-03-03 12:25:10');
set foreign_key_checks =1;
alter user 'root'@'localhost' identified by '280939'
flush privileges ;