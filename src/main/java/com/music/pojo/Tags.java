package com.music.pojo;

import com.github.yulichang.annotation.Table;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table("tags_dictionary")
public class Tags {

        private static final long serialVersionUID = 1L;

        private Integer tagId;      // 对应 tag_id (主键)
        private String tagName;     // 对应 tag_name (标签名)
        private Integer useCount;   // 对应 use_count (使用次数，可选)



}
