package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserTags {
    private  int UTId;
    private int userId;
    private int tagId;
    private LocalDateTime createTime;
    private int status;
}
