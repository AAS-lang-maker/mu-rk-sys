package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor  // 必须：Jackson 需要无参构造
@AllArgsConstructor
public class PersonalRank {
    private Integer rankId;
    private String rankName;
    private Integer userId;
    private Integer categoryId;
    private Integer voteCount;
    private Date publishTime;
    private Integer targetId;
}
