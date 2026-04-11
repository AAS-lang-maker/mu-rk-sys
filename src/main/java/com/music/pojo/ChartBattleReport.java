package com.music.pojo;

import java.io.Serializable;

public class ChartBattleReport  implements Serializable {
    private static final long serialVersionUID = 1L;


    private Long songId;          // 歌曲ID
    private String songName;      // 歌曲名
    private String singer;    // 歌手名（
    private String songImg;      // 封面图
    private Long   score;
    private Integer currentRank;  // 当前排名（如：1）
    private Integer rankChange;   // 排名变化（如：1 表示上升1名，0表示不变，-1表示下降）

    private Long surpassedSongId;     // 被超越的歌曲ID
    private String surpassedSongName; // 被超越的歌曲名（用于文案：“超越了《XXX》”）
    private Integer surpassedRank;    // 被超越时的排名（通常是当前排名，或者是上一名的排名）

    private Long timestamp;           // 战报生成时间戳
    private String reportType;        // 战报类型：OVERTAKE(超越), NEW_ENTRY(新上榜), TOP_ACHIEVED(达成成就)

}
