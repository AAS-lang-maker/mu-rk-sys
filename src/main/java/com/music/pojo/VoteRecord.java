package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VoteRecord {
    private Integer voteId;
    private Integer userId;
    private Integer vote;
    private String ip;
    private Integer voteStatus;
    private Integer rankId;
    private LocalDateTime voteTime;
}
