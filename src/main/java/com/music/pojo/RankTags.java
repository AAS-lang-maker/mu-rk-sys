package com.music.pojo;

import lombok.Data;

@Data
public class RankTags {
  private int RTId;
  private int rankId;
  private int userId;
  private int tagId;
  private Data createTime;
}
