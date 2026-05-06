package com.music.Service.impl;

import com.music.Mapper.SongMapper;
import com.music.Service.SongService;
import com.music.pojo.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class SongServiceImpl implements SongService {
    @Autowired
    private SongMapper songMapper;

    @Value("${music.file.path}")
    private String musicFilePath;

    @Override
    public Song getSongById(Integer songId) {
        Song song = songMapper.selectSongById(songId);
        if (song == null) {
            throw new RuntimeException("未找到ID为" + songId + "的歌曲");
        }
        return song;
    }

    @Override
    public File getSongFile(Song song) {
        String songUrl = song.getSongurl();
        String fullurl = musicFilePath + songUrl.trim();
        File mp3file = new File(fullurl);
        if (!mp3file.exists()) {
            throw new RuntimeException("音频文件不存在：" + fullurl);
        }
        if (!mp3file.isFile()) {
            throw new RuntimeException("路径不是有效文件：" + fullurl);
        }
        return mp3file;
    }
}