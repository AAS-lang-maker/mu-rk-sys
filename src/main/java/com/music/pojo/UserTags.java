package com.music.pojo;

import com.github.yulichang.annotation.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@Table("user_tags")
public class UserTags {
    private  int UTId;
    private int userId;
    private int tagId;
    private LocalDateTime createTime;
    private int status;
    private Integer useCount;

}
