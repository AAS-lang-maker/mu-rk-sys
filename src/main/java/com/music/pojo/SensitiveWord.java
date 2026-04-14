package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensitiveWord {
    private int abcId;
    private String sComment;
    private int comId;
    private LocalDateTime createTime;
}
