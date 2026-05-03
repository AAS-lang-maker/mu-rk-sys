package com.music.pojo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserTagVO {

    private  int UTId;
    private int userId;
    private int tagId;
    private LocalDateTime createTime;
    private int status;
    private Integer useCount;

    private String tagName;
}
