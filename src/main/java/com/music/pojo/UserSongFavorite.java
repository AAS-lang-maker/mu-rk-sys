package com.music.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserSongFavorite {
    private int USFID;
    private int userId;
    private int songId;
    private LocalDateTime createdAt;
}
