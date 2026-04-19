package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor  // 必须：Jackson 需要无参构造
@AllArgsConstructor
public class RankTags {
  private int RTId;
  private int rankId;
  private int userId;
  private int tagId;
  private Date createTime;
}
