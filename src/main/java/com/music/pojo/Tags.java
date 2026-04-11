package com.music.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tags {

        private static final long serialVersionUID = 1L;

        private Integer tagId;      // 对应 tag_id (主键)
        private String tagName;     // 对应 tag_name (标签名)
        private Integer useCount;   // 对应 use_count (使用次数，可选)



}
