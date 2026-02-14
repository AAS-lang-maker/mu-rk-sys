package com.music.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVo {
    private Integer comId;
    private String comContent;
    private Integer userId;
    private Integer rankId;
    private String username;
    private LocalDateTime comTime;
    private Integer parentId;
    private Integer likeCount;
    private Integer isLiked;
    private String parentUsername;
}
