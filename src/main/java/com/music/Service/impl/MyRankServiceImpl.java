package com.music.Service.impl;

import com.github.pagehelper.PageInfo;
import com.music.Mapper.MyRankMapper;
import com.music.Mapper.TagsMapper;
import com.music.Service.MyRankService;
import com.music.dto.CommentVo;
import com.music.dto.EditRank;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.*;
import com.music.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.util.ArrayList;
import java.util.List;

@Service
public class MyRankServiceImpl implements MyRankService {
@Autowired
private MyRankMapper myRankMapper;
//private static final Logger log = (Logger) LogManager.getLogger(MyRankServiceImpl.class);
    //查询personl_rank主表
    @Autowired
    private TagsMapper tagsMapper;
    @Override
    public PageInfo<MyRankWithSong> selectMyrank(Integer pageNum,Integer pageSize,Integer offset,Integer userId) {
        //逻辑：从Mapper到Service，最后到Controller
        //Mapper层查询，将所有相关数据返回给MyRankWithSong这个集合中，Service层把这些数据由get->set传给Controller的PageInfo
        //pageInfo再传给前端代码
      List<MyRankWithSong> list =myRankMapper.selectMyRank(userId,pageSize,offset);
      System.out.println("Mapper查询到的榜单数："+list.size());
      Integer total = myRankMapper.selectMyRankTotal(userId);
      Integer pages=(total+pageSize-1)/pageSize;
       PageInfo<MyRankWithSong> pageInfo=new PageInfo<>();
       pageInfo.setList(list);
       pageInfo.setPageNum(pageNum);
       pageInfo.setPageSize(pageSize);
       pageInfo.setTotal(total);
       pageInfo.setPages(pages);
          return pageInfo;
    }

    @Override
    public List<Singer> selectSinger(Integer categoryId) {
        return myRankMapper.selectSinger(categoryId);
    }

    @Override
    public List<Song> selectSong(Integer singerId) {
        return myRankMapper.selectSong(singerId);
    }


    @Override
    public MyRankWithSong getRank(Integer rankId) {

        System.out.println("看榜单详情榜单："+rankId);

        Integer userId= ThreadLocalUtil.get();

        System.out.println("用户id："+userId);

        List<Tags>tagVOList=tagsMapper.selectRankTag(rankId,userId);

        RankTagVO rankTagVO=new RankTagVO(rankId,tagVOList);

        MyRankWithSong myRankWithSong=myRankMapper.getRank(rankId);

        myRankWithSong.setRankTagVOList(rankTagVO);
        return myRankWithSong;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(EditRank dto, Integer rankId) {
        String rankName=dto.getRankName();
        //log.info("开始编辑榜单：rankId={}, 新名称={}", rankId, rankName);
        myRankMapper.insertNewRankname(rankId,rankName);
        //log.info("主表榜单名更新完成");
        //log.info("删除子表旧数据：rankId={}, 删除行数={}", rankId, deleteCount);
        List<RankAddRequest.RankSongItem> items=dto.getSongItems();
        List<RankSong> newSongs=new ArrayList<>();
        if(!CollectionUtils.isEmpty(items)){
            for (RankAddRequest.RankSongItem item : items) {
                RankSong newSong=new RankSong();
                newSong.setSongId(item.getSongId());
                newSong.setRanking(item.getRanking());
                newSong.setRankId(rankId);
                newSongs.add(newSong);
            }
        }
        if(!CollectionUtils.isEmpty(newSongs)){
          //  log.info("开始插入新歌曲：rankId={}, 歌曲数量={}", rankId, newSongs.size());
            myRankMapper.insertNewRank(newSongs);
            //log.info("新歌曲插入完成");
        }else {
            //log.warn("无有效歌曲数据，跳过插入");
        }
        return true;
    }

    @Override
    public PageInfo<MyRankWithSong> selectMyLoverank(Integer pageNum, Integer pageSize, Integer offset, Integer userId) {
        List<MyRankWithSong> myLoveSong=myRankMapper.selectLoveSong(userId,pageSize,offset,pageNum);
        PageInfo<MyRankWithSong> pageInfo=new PageInfo<>();
        Integer total=myRankMapper.selectloveTotal(userId);
        Integer pages=(total+pageSize-1)/pageSize;
        pageInfo.setList(myLoveSong);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        System.out.println("查询出的喜欢的榜单数："+pageInfo.getList().size());
        return pageInfo;
    }



    @Override
    public UserInfo selectByuserId(Integer userId) {
        return myRankMapper.selectByUserId(userId);
    }

    @Override
    public List<CommentVo> selectComment(Integer rankId, Integer userId) {
        List<CommentVo> list=myRankMapper.selectComment(rankId,userId);
        System.out.println("查询出的评论数："+list.size());
        return list;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer comId,Integer userId) {
        Comment comment=myRankMapper.selectCommentById(comId);
        if(comment==null){
            return false;
        }
        if(comment.getUserId()!=userId){
            throw new RuntimeException("只能删除自己的评论");
        }
        int result=myRankMapper.updateComment(comId,1);
        return result>0;
    }
}
