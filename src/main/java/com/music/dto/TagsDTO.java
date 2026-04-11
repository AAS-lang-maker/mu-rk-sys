package com.music.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagsDTO implements Serializable {


    private Integer tagId;      // 对应 tag_id (主键)
    private String tagName;     // 对应 tag_name (标签名)
    private Integer useCount;   // 对应 use_count (使用次数，可选)



}
