package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFollow {
    private int UFId;
    private int userId;
    private int followId;
    private LocalDateTime followTime;
    private int isMutual;
}
