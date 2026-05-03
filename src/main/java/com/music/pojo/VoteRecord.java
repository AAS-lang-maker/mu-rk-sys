package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoteRecord {
    private Integer voteId;
    private Integer userId;
    private String ip;
    private Integer rankId;
    private Integer songId;
    private LocalDateTime voteTime;
}
