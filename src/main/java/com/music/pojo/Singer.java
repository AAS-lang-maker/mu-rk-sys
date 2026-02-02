package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Singer {
    private Integer singerId;
    private String singerName;
    private String singerImage;
    private Integer categoryId;
    private LocalDateTime singerCreateTime;
    private Integer singerStatus;
    private Integer singerSort;
}
