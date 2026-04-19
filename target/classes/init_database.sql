create database  music_db character set utf8mb4;
USE music_db;
create table category(
                         category_id int primary key auto_increment comment "分类id",
                         categoty_name varchar(50) not null unique comment "分类名称",
                         category_status tinyint default 1 comment "状态id(1为启用，0为禁用)",
                         catrgory_sort int default 0 comment "前端展示顺序"
)comment "榜单分类表" character set utf8mb4;
create table singer(
                       singer_id int primary key auto_increment comment "歌手id",
                       singer_name varchar(100) not null  comment "歌手名",
                       singer_img varchar(255) comment "歌手图片",  -- 存储图片以URL形式，最多不超过255字符
                       category_id int not null  comment "分类id",
                       singer_create_time datetime default current_timestamp comment "歌手创建时间",
                       singer_status tinyint default 1 comment "状态id(1为启用，0为禁用",
                       singer_sort int default 0 comment "歌手前端展示排序",
                       foreign key (category_id) references category(category_id)
)comment "歌手信息表" character set utf8mb4;
create table work(
                     work_id int primary key auto_increment comment "影视及游戏作品id",
                     work_name varchar(100) not null comment "影视及游戏作品名",
                     work_img varchar(255) comment "相关图片",
                     category_id int not null comment "分类id",
                     foreign key (category_id) references category(category_id),
                     work_create_time datetime default current_timestamp comment "作品创建时间",
                     work_status tinyint default 1 comment "状态id(1为启用，0为禁用",
                     work_sort int default 0 comment "影视及游戏作品前端展示排序"
)comment "影视及游戏信息表" character set utf8mb4;
create table song(
                     song_id int primary key auto_increment comment "歌曲id",
                     song_name varchar(50) not null comment "歌曲名",
                     song_img varchar(255) comment "相关图片",
                     song_create_time datetime default current_timestamp comment "歌曲添加时间",
                     song_status tinyint default 1 comment "状态id(1为启用，0为禁用)",
                     singer_id int comment "关联歌手表",
                     work_id int comment "关联作品表",
                     check ( (singer_id is not null and work_id is null) or work_id is not null and singer_id is null),
    -- 关联的分类必须是二选一
                     foreign key(singer_id) references singer (singer_id),
                     foreign key (work_id) references work (work_id)
)comment "总歌曲表" character set utf8mb4;
create table user(
                     user_id int primary key auto_increment,
                     username varchar(50) not null comment "用户名",
                     user_create_time datetime default current_timestamp comment "注册时间"
)comment "用户表" character set utf8mb4;
create table personal_rank(
                              rank_id int primary key auto_increment comment "个人榜单id",
                              rank_name varchar(50) not null comment "榜单名",
                              user_id int not null comment "关联用户id",
                              category_id int not null comment "分类id",
                              vote_count int default 0 comment "总支持数",
                              publish_time datetime default current_timestamp comment "个人榜单发布时间",
                              target_id int not null comment "分类下的具体歌手或作品id",
                              foreign key (category_id) references category (category_id),
                              foreign key (user_id) references user (user_id)
)comment "个人榜单表" character set utf8mb4;   -- 用户表和personal——rank结合实现一个用户可以发布多个人榜单
create table rank_song(
                          rs_id int primary key auto_increment comment "榜单歌曲id主键",
                          rank_id int not null comment "关联个人榜单表",
                          song_id int not null comment "关联总歌曲表",
                          ranking int not null comment "歌曲排名",
                          rank_create_time datetime default current_timestamp comment "创建时间",
                          rank_update_time datetime default current_timestamp on update current_timestamp comment "更新时间",
                          unique key ur_song(rank_id,song_id),  -- 唯一外键约束，保证一个歌曲排名中一首歌只能出现一次
                          foreign key (rank_id) references personal_rank (rank_id),
                          foreign key (song_id) references song (song_id)
)comment "榜单歌曲排名表" character set utf8mb4;
create table vote_record
(
    vote_id     int primary key auto_increment comment "投票记录id",
    user_id     int comment "用户id", -- 可为空，支持匿名投票
    vote        tinyint  not null comment "投票类型(1为支持，0为反对)",
    ip          varchar(50) not null comment "投票用户ip",
    vote_status tinyint  default 1 comment "状态id(1为启用，0为禁用)",
    rank_id     int not null comment "关联个人榜单",
    vote_time   datetime default current_timestamp comment "投票时间",
    foreign key (user_id) references user (user_id),
    foreign key (rank_id) references personal_rank (rank_id),
    unique key vp_ip (rank_id, ip)    -- 唯一外键，一个ip只能投一个榜单的票，防止一个用户开超多小号刷一万个票
)comment "投票记录表" character set utf8mb4;
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
show create table comment;
show create table like_comment;
show create table love_record;
show create table user;
show create table category;
show create table personal_rank;
show create table rank_song;
show create table singer;
show create table song;
show create table vote_record;
show create table work;
alter table user add column role tinyint not null default 0 comment '0为普通用户，1为管理员';
create table sensitive_word(
    adc_id int primary key auto_increment comment '敏感词管理主键id',
    s_comment varchar(500) comment '敏感词内容',
    com_id int not null comment '外键关联评论区id',
    create_time datetime default current_timestamp comment '时间',
    foreign key (com_id) references comment (com_id)
)comment "敏感词审核" character set utf8mb4;
alter table comment add column is_ai tinyint not null default 0 comment '0为真人评论，1为AI锐评';
alter table comment add column status tinyint not null default 0 comment '0是未审核状态，1是已审核';
ALTER TABLE `comment`
    ADD COLUMN `report_flag` TINYINT DEFAULT 0 COMMENT '是否有被举报: 0-无, 1-有';
CREATE TABLE `ai_chat_log` (
                               `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                               `user_id` INT NOT NULL,
                               `message` TEXT NOT NULL,
                               `role` VARCHAR(20) NOT NULL COMMENT 'user 或 assistant',
                               `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
)comment "AI智能体聊天日志" character set utf8mb4;

-- 2. 记录用户偏好的“小账本”
CREATE TABLE `user_behavior` (
                                 `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
                                 `UT_id` int comment '用户个人标签',
                                 `song_id` INT NOT NULL,
                                 `play_count` INT DEFAULT 1,
                                 `ST_id` int COMMENT '用户建立的歌曲标签',
                                 `user_id` int comment '用户的榜单id', -- 这里改成了反引号
                                 `last_play_time` datetime default CURRENT_TIMESTAMP ON update CURRENT_TIMESTAMP
) comment "用户行为表" character set utf8mb4;

CREATE TABLE user_follow (
                             UF_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                             user_id INT NOT NULL COMMENT '粉丝ID',
                             follow_id INT NOT NULL COMMENT '偶像ID',
                             create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
                             UNIQUE KEY uk_user_follow (user_id, follow_id) USING BTREE COMMENT '联合唯一索引，防止重复关注',
                             is_mutual TINYINT NOT NULL DEFAULT 0 COMMENT '是否互粉: 1=是, 0=否'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';

CREATE TABLE Song_Tags (
                           ST_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                           song_id INT NOT NULL COMMENT '歌曲ID',
                           user_id INT NOT NULL COMMENT '打标签的用户ID',
                           tag_id INT NOT NULL COMMENT '关联字典表的ID',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打标签时间',
                           INDEX idx_song_id (song_id),
                           INDEX idx_user_id (user_id),
                           INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲标签表';

-- 可选：删除用户时自动删除相关标签（可通过外键ON DELETE CASCADE实现）
CREATE TABLE User_Tags (
                           UT_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                           user_id INT NOT NULL COMMENT '打标签的用户ID',
                           tag_id INT NOT NULL COMMENT '关联字典表的ID',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打标签时间',
                           status INT DEFAULT 0 COMMENT '标签状态 (0正常，1审核中，2屏蔽)',
                           INDEX idx_user_id (user_id),
                           INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲标签表';

CREATE TABLE `user_song_favorite` (
                                      `USF_id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
                                      `user_id` int unsigned NOT NULL COMMENT '用户ID',
                                      `song_id` int unsigned NOT NULL COMMENT '歌曲ID',
                                      `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
                                      PRIMARY KEY (`USF_id`),
                                      UNIQUE KEY `uk_user_song` (`user_id`,`song_id`) COMMENT '防止重复收藏'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE Tags_Dictionary (
                                 tag_id INT PRIMARY KEY AUTO_INCREMENT,
                                 tag_name VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名',
                                 use_count INT DEFAULT 0 COMMENT '使用次数（用于排序）',
                                 INDEX idx_name (tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
alter table user_behavior change id UB_id bigint;
alter table ai_chat_log change id AI_id bigint;
alter table comment add column risk_score int;
alter table comment add column audit_remark text;
ALTER TABLE comment DROP COLUMN audit_remark;
alter table comment add column audit_remark text;
create table song_demand(
    demand_id int primary key auto_increment comment '主键id',
    demand_songname varchar(500) not null comment '申请歌曲',
    demand_singername varchar(500) not null comment '歌曲对应歌手'
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲申请需求表';
alter table  song_demand add column is_delete int default 0;
alter table song_demand add column create_time datetime default current_timestamp;
select user.username from user
CREATE TABLE Rank_Tags (
                           RT_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
                           rank_id BIGINT NOT NULL COMMENT '榜单ID',
                           user_id BIGINT NOT NULL COMMENT '打标签的用户ID',
                           tag_id  BIGINT NOT NULL COMMENT '标签内容',
                           create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
                           INDEX idx_song_id (rank_id),
                           INDEX idx_user_id (user_id),
                           INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='榜单标签表';