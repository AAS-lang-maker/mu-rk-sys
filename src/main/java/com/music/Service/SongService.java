package com.music.Service;

import com.music.pojo.Song;
import java.io.File;

public interface SongService {
 /**
  * 根据歌曲ID获取歌曲信息和对应的音频文件
  */
 Song getSongById(Integer songId);

 /**
  * 获取歌曲对应的音频文件
  */
 File getSongFile(Song song);
}
