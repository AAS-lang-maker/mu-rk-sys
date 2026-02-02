package com.music.dto;

import lombok.Data;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.List;

@Data
public class RankAddRequest {
    //结合用户的输入操作
    private String rankName;
    private Integer targetId;
    private List<RankSongItem> songItems;
    @Data
    public static class RankSongItem{
        /*前端逻辑：用户不会直接输入歌曲名,而是查找相应歌手歌曲名并选择的形式去传入歌曲名
        1.歌曲名很可能重复，而导致不知道到底是哪个歌曲
        2.保证数据准确性*/
        private Integer songId;
        private Integer ranking;
    }
}
