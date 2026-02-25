package com.music.Service.impl;

import com.github.pagehelper.PageInfo;
import com.music.Mapper.UserPublishMapper;
import com.music.Service.UserPublishService;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.dto.RankAddRequest;
import com.music.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service

public class userPublishServiceImpl implements UserPublishService {
    private static final Logger log = LoggerFactory.getLogger(userPublishServiceImpl.class);
    @Autowired
    private UserPublishMapper userPublishMapper;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    private static final String Love_Key="love:user:%d:rank:%d";
    private static final String Vote_Key="vote:user:%d:rank:%d";

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)//事务注解:主表和子表的Service层操作必须同时成功，否则就是失败
    public boolean insertRank(Integer categoryId, Integer userId, RankAddRequest rankAddRequestDto) {
       //？依旧难想，将Dto类转化为对应数据库的实体类
        //先创建一个pojo对应的对象，再将前端dto的对应值传进去，最后调用Mapper层
        PersonalRank personalRank = new PersonalRank();
        personalRank.setCategoryId(categoryId);
        personalRank.setUserId(userId);
        //细心豆包，数据库里面默认target——id为空，所以当它真的为空，就设置其值为0
        personalRank.setTargetId(rankAddRequestDto.getTargetId() == null ? 0 : rankAddRequestDto.getTargetId());
        personalRank.setRankName(rankAddRequestDto.getRankName());
        userPublishMapper.insertRank(personalRank);
        //主表和子表的外键关联要拿出来（666）
        Integer rankId = personalRank.getRankId();
        System.out.println("获取的rankId：" + rankId);
        if (rankId == null) {
            System.out.println("rankId为null，直接返回失败"); // 新增：打印跳过逻辑
            return false;
        }

        List<RankSong> ranksongList=new ArrayList<RankSong>();//用集合接受前端榜单数据，因为歌曲和排名有很多，一个用户还可能有多个榜单
        //豆包大人教我写最难写的lamda？？表达式
        rankAddRequestDto.getSongItems().forEach(item->{
            RankSong rankSong=new RankSong();
            rankSong.setRankId(rankId);
            rankSong.setRanking(item.getRanking());
            rankSong.setSongId(item.getSongId());
            ranksongList.add(rankSong);
        });
        int insertCount = userPublishMapper.sixInsert(ranksongList);
        if (insertCount != ranksongList.size()) {
            System.out.println("子表插入行数与预期不符，返回失败");
            return false; // 触发事务回滚
        }
        return  true;
        }

    @Override
    public List<Singer> selectSinger(Integer categoryId) {
        List<Singer> singers=userPublishMapper.selectSinger(categoryId);
        return singers;
    }

    @Override
    public List<Song> selectSong(Integer singerId) {
        List<Song> songs=userPublishMapper.selectSong(singerId);
        return songs;
    }

    @Override
    public PageInfo<MyRankWithSong> selectPublishRank(Integer category, Integer pageNum, Integer pageSize, Integer offset) {
        List<MyRankWithSong> ranks=userPublishMapper.selectPublishRank(category,pageSize,offset);
        PageInfo<MyRankWithSong> pageInfo=new PageInfo();
        Integer total=userPublishMapper.selectTotal(category);
        Integer pages=(total+pageSize-1)/pageSize;
        System.out.println("所有榜单："+ranks.size());
        pageInfo.setList(ranks);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setPages(pages);
        return pageInfo;
    }

    @Override
    @Transactional
    public boolean insertVote(Integer userId, Integer rankId) {
        String votekey=String.format(Vote_Key,userId,rankId);
        log.info("【点赞操作】开始，userId={}, rankId={}, RedisKey={}", userId, rankId, votekey);
        Boolean result=stringRedisTemplate.hasKey(votekey);
        if(Boolean.TRUE.equals(result)){
            log.info("【取消点赞】Redis已存在记录，准备删除数据库记录");
            boolean deleteSuccess=stringRedisTemplate.delete(votekey);
            if (!deleteSuccess) {
                log.warn("【取消投票】第一次删除Redis Key失败，重试一次！Key={}", votekey);
                deleteSuccess = stringRedisTemplate.delete(votekey);
            }

            // 打印删除结果（关键：看日志就能知道删没删成）
            log.info("【取消投票】Redis Key删除结果：{}（true=成功，false=失败），Key={}", deleteSuccess, votekey);

            int deleteresult=userPublishMapper.deleteVote(userId,rankId);
            log.info("【取消投票】数据库影响行数：{}", deleteresult);
                return deleteresult>0;

        }
        PersonalRank v=userPublishMapper.selectRankById(rankId);
        if(v==null){
            return false;
        }
        int rows=userPublishMapper.insertVote(rankId,userId);
        stringRedisTemplate.opsForValue().set(votekey,"1");
        log.info("【投票成功】Redis已记录，userId={}, rankId={}", userId, rankId);
        return rows>0;
    }

    @Override
    @Transactional
    public boolean insertLove(Integer userId,  Integer rankId) {
         //拼接Redis的key值
        String lovekey=String.format(Love_Key,userId,rankId);
        log.info("【收藏操作】开始，userId={}, rankId={}, RedisKey={}", userId, rankId, lovekey);
       //看看Redis中是否有key，如果有，就不能再点赞，从而达到防刷票
        Boolean hasLove=stringRedisTemplate.hasKey(lovekey);
        if(Boolean.TRUE.equals(hasLove)){
            log.info("【取消收藏】Redis已存在记录，准备删除数据库记录");
            int result=userPublishMapper.deleteLove(userId,rankId);
            log.info("【取消收藏】Redis记录已删除，userId={}, rankId={}", userId, rankId);
                stringRedisTemplate.delete(lovekey);
            log.info("【取消收藏】mysql记录已删除，userId={}, rankId={}", userId, rankId);
                return result>0;
        }
        PersonalRank l=userPublishMapper.selectRankById(rankId);
       if(l==null){
           return false;
       }
        int row1=userPublishMapper.insertLove(userId,rankId);
        if(row1>0){
         stringRedisTemplate.opsForValue().set(lovekey,"1");//1??"1"?
        log.info("【收藏成功】Redis已记录，userId={}, rankId={}", userId, rankId);
        return row1>0;}
        else{
            log.error("【收藏成功】Redis记录失败或者数据库操作失败，userId={}, rankId={}", userId, rankId);
            return false;
        }
    }

    @Override
    public PageInfo<MyRankWithSong> selectSearch(Integer category, Integer pageNum,
                                                 Integer offset, Integer pageSize,String keyword) {
        if(keyword!=null){
            keyword=keyword.trim();
        }else{
            keyword=null;
        }
        List<MyRankWithSong> list=userPublishMapper.selectSearch(category,pageNum,offset,pageSize,keyword);
        PageInfo<MyRankWithSong> searchRank=new PageInfo();
        Integer total=userPublishMapper.selectSearchTotal(category,keyword);
        Integer pages=(total+pageSize-1)/pageSize;
        searchRank.setList(list);
        searchRank.setPageNum(pageNum);
        searchRank.setPageSize(pageSize);
        searchRank.setTotal(total);
        searchRank.setPages(pages);
        return searchRank;
    }

    @Override
    public Comment insertComment(Integer rankId, Integer userId, String content, Integer parentId) {
        Comment comment=new Comment();
        comment.setRankId(rankId);
        comment.setUserId(userId);
        comment.setComContent(content);
        comment.setParentId(parentId);
        userPublishMapper.insertComment(rankId,userId,content,parentId);
        return  comment;
    }

    @Override
    public List<CommentVo> selectComment(Integer rankId, Integer userId) {
       List<CommentVo> list=userPublishMapper.selectComment(rankId,userId);
       System.out.println("查询出的评论数："+list.size());
       return list;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer comId,Integer userId) {
        Comment comment=userPublishMapper.selectCommentById(comId);
        if(comment==null){
            return false;
        }
        if(comment.getUserId()!=userId){
            throw new RuntimeException("只能删除自己的评论");
        }
        int result= userPublishMapper.updateComment(comId,1);
        return result>0;
    }

    @Override
    @Transactional
    public boolean insertLike(Integer userId,Integer comId) {
       Comment comment=userPublishMapper.selectCommentById(comId);
       if(comment==null||comment.getIdDelete()==1){
           return false;
       }
       int result=userPublishMapper.insertLike(userId,comId);
       return result>0;
    }

    @Override
    @Transactional
    public boolean deleteLike(Integer comId, Integer userId) {
      Comment comment=userPublishMapper.selectCommentById(comId);
      if(comment==null||comment.getIdDelete()==1){
          return false;
      }
      int result=userPublishMapper.deleteLike(comId,userId);
      return result>0;
    }

    @Override
    @Transactional
    public boolean deleteVote(Integer rankId, Integer userId) {
        // 原有逻辑
        PersonalRank personalRank = userPublishMapper.selectRankById(rankId);
        if (personalRank == null) {
            // 新增日志：打印榜单不存在的提示
            log.warn("【取消点赞】榜单不存在！rankId={}, userId={}", rankId, userId);
            return false;
        }

        // 新增日志：打印要删除的条件
        log.info("【取消点赞】准备删除点赞记录：rankId={}, userId={}", rankId, userId);
        int result = userPublishMapper.deleteVote(rankId, userId);

        // 新增日志：打印删除结果（核心！看删了多少行）
        log.info("【取消点赞】删除操作完成，影响行数：{}", result);

        return result > 0;
    }

    // 你的deleteLove方法（同理加日志）
    @Override
    @Transactional
    public boolean deleteLove(Integer rankId, Integer userId) {
        PersonalRank personalRank = userPublishMapper.selectRankById(rankId);
        if (personalRank == null) {
            log.warn("【取消收藏】榜单不存在！rankId={}, userId={}", rankId, userId);
            return false;
        }

        log.info("【取消收藏】准备删除收藏记录：rankId={}, userId={}", rankId, userId);
        int result = userPublishMapper.deleteLove(rankId, userId);
        log.info("【取消收藏】删除操作完成，影响行数：{}", result);

        return result > 0;
    }

    //豆包更新之后变权威了
    //下方方法目的：刷新页面之后不会让统统一用户能重复投票，使用redis达到记录用户投票状态的目的
    private void sychorVoteToRedis(Integer rankId,Integer userId,String votekey){
        //sychronise翻译为同步，有点像锁但不是，这里本质是一个方法，将redis和mysql同步
        int x=userPublishMapper.countVote(rankId,userId);
        boolean flag=stringRedisTemplate.hasKey(votekey);
        if(x>0||flag==false){
           stringRedisTemplate.opsForValue().set(votekey,"1");
            log.info("【状态同步】数据库有投票记录，Redis补写Key：{}", votekey);
        }
        if(x<=0||flag==true){
            stringRedisTemplate.delete(votekey);
            log.info("【状态同步】数据库无投票记录，Redis删除Key：{}", votekey);
        }
    }
    private void sychorLoveToRedis(Integer rankId,Integer userId,String lovekey){
        int y=userPublishMapper.countLove(rankId,userId);
        boolean flag=stringRedisTemplate.hasKey(lovekey);
        if(y>0||flag==false){
            stringRedisTemplate.opsForValue().set(lovekey,"1");
            log.info("【状态同步】数据库有投票记录，Redis补写Key：{}", lovekey);
        }
        if(y<=0||flag==true){
            stringRedisTemplate.delete(lovekey);
            log.info("【状态同步】数据库无投票记录，Redis删除Key：{}", lovekey);
        }
    }
}
