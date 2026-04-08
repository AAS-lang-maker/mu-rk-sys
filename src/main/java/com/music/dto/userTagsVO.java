package com.music.dto;

import lombok.Data;

@Data
public class userTagsVO {
private Integer tagId;
private String tagName;
private Integer userId;
private Integer createTime;
private Integer status;
}
