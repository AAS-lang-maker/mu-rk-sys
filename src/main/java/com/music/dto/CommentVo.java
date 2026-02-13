package com.music.dto;

import lombok.Data;

@Data
public class CommentVo {
    private String comId;
    private String comContent;
    private Integer userId;
    private Integer rankId;
}
