package com.music.dto;

import lombok.*;

import java.io.Serializable;

@Data

public class TagsPageQueryDTO implements Serializable {
    private Integer pageNum = 1;
    private Integer pageSize = 10; // 默认一页10个，前端卡片流可能一页要20个

    // 👇 新增：搜索关键词
    private String tagName;

    private Integer limitCount;

    // 👇 新增：排序方式（按热度？按时间？）
    private String sortBy = "use_count"; // 默认按热度排，让大标签排前面
    private String sortOrder = "desc";
}
