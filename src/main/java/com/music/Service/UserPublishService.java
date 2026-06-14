package com.music.Service;

import com.github.pagehelper.PageInfo;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.Comment;
import com.music.pojo.Singer;
import com.music.pojo.Song;
import java.util.List;


public interface UserPublishService {
    public Integer insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto);

    List<Singer> selectSinger(Integer categoryId);

    List<Song> selectSong(Integer singerId);

    PageInfo<MyRankWithSong> selectPublishRank(Integer category, Integer pageNum, Integer pageSize, Integer offset);

    boolean insertVote(Integer userId, Integer rankId);

    boolean insertLove(Integer userId, Integer rankId);

    PageInfo<MyRankWithSong> selectSearch(Integer category, Integer pageNum, Integer offset, Integer pageSize,String keyword);

    Comment insertComment(Integer rankId, Integer userId, String content, Integer parentId);

    List<CommentVo> selectComment(Integer rankId, Integer userId);

    boolean deleteComment(Integer comId,Integer userId);

    boolean insertLike(Integer userId,Integer comId);

    boolean deleteLike(Integer comId,Integer userId);

    boolean deleteVote(Integer rankId, Integer userId);

    boolean deleteLove(Integer rankId, Integer userId);

    boolean insertApply(String singerName, String songName);

}
