package com.music.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.music.dto.songQuery;
import com.music.pojo.Song;
import com.music.result.PagedTO;


import java.io.File;
import java.util.List;

public interface SongService extends IService<Song> {
 /**
  * 根据歌曲ID获取歌曲信息和对应的音频文件
  */
 Song getSongById(Integer songId);

 /**
  * 获取歌曲对应的音频文件
  */
 File getSongFile(Song song);

   PagedTO<Song> page1(songQuery songQu);
}
