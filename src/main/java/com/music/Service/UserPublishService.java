package com.music.Service;

import com.music.dto.RankAddRequest;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import com.music.utils.Result;

import java.util.List;


public interface UserPublishService {
    boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) ;

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);
}
