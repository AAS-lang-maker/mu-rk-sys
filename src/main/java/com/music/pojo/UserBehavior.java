package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBehavior {
    private int UBId;
    private int UTId;
    private int songId;
    private int playCount;
    private int STId;
    private int userId;
    private LocalDateTime lastPlayTime;
}
