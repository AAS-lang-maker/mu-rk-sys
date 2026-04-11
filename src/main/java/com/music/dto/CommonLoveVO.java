package com.music.dto;

import lombok.Data;

import java.util.List;

@Data
public class CommonLoveVO {
    /**
     * 互关好友中，收藏过该榜单的人数
     */
    private Integer commonLoveCount;

    /**
     * 互关好友中收藏过该榜单的用户名（可用于弹窗展示）
     */
    private List<String> usernames;
}

