package com.music.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RankTag implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer RTId;
    private Integer tagId;
    private Integer rankId;
    private Integer userId;
    private LocalDateTime createTime;

}
