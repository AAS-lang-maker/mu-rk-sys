package com.music.pojo;

import com.alibaba.druid.support.json.JSONWriter;
import lombok.Data;

import java.util.Date;

@Data
public class UserInterestVetor {
   private int userInterestVetorId;
   private int userId;
   private JSONWriter styleTags;
   private Date lastUpdateTime;
}
