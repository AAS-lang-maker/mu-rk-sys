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
