package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Work {
     private  String workId;
     private String categoryId;
     private String workName;
     private LocalDateTime workTime;
     private Integer workSort;
     private Integer workStatus;
}
