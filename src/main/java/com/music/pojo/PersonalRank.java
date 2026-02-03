package com.music.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class PersonalRank {
    private Integer rankId;
    private String rankName;
    private Integer userId;
    private Integer categoryId;
    private Integer voteCount;
    private Date publishTime;
    private Integer targetId;
}
