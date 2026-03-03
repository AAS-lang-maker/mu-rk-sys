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
import java.util.concurrent.TimeUnit;

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
    @Transactional(rollbackFor = Exception.class) // 明确回滚所有异常
    public boolean insertVote(Integer userId, Integer rankId) {
        // 1. 校验基础参数（避免空指针）
        if (userId == null || rankId == null) {
            log.error("【点赞操作】参数为空，userId={}, rankId={}", userId, rankId);
            return false;
        }
        String voteKey = String.format(Vote_Key, userId, rankId);
        log.info("【点赞操作】开始，userId={}, rankId={}, RedisKey={}", userId, rankId, voteKey);

        // 2. 核心：用Redis的setIfAbsent原子操作判断是否已投票（解决竞态条件）
        // setIfAbsent = 只有key不存在时才设置成功，返回true；已存在则返回false
        Boolean isFirstVote = stringRedisTemplate.opsForValue().setIfAbsent(voteKey, "1", 24, TimeUnit.HOURS);

        // ========== 分支1：已投票（需要取消点赞） ==========
        if (Boolean.FALSE.equals(isFirstVote)) {
            log.info("【取消点赞】Redis已存在记录，开始删除数据库+Redis记录");
            // 第一步：先删数据库（保证数据一致性，删库成功再删Redis）
            int deleteResult = userPublishMapper.deleteVote(rankId, userId);
            if (deleteResult <= 0) {
                log.error("【取消点赞】数据库删除失败，userId={}, rankId={}", userId, rankId);
                return false; // 数据库没删成，直接返回失败
            }
            log.info("【取消点赞】数据库记录已删除，影响行数：{}", deleteResult);

            // 第二步：删Redis（重试机制保留）
            boolean deleteSuccess = stringRedisTemplate.delete(voteKey);
            if (!deleteSuccess) {
                log.warn("【取消点赞】第一次删除Redis Key失败，重试一次！Key={}", voteKey);
                deleteSuccess = stringRedisTemplate.delete(voteKey);
            }
            log.info("【取消点赞】Redis Key删除结果：{}，Key={}", deleteSuccess, voteKey);

            return deleteSuccess; // 最终返回Redis删除结果（保证缓存和数据库一致）
        }

        // ========== 分支2：未投票（需要新增点赞） ==========
        // 先校验榜单是否存在
        PersonalRank v = userPublishMapper.selectRankById(rankId);
        if (v == null) {
            log.error("【点赞操作】榜单不存在，rankId={}", rankId);
            stringRedisTemplate.delete(voteKey); // 删掉刚才原子操作的占位key
            return false;
        }

        // 插入数据库
        int rows = userPublishMapper.insertVote(rankId, userId);
        if (rows <= 0) {
            log.error("【点赞操作】数据库插入失败，userId={}, rankId={}", userId, rankId);
            stringRedisTemplate.delete(voteKey); // 数据库失败，删Redis占位key
            return false;
        }

        log.info("【点赞成功】数据库和Redis均已记录，userId={}, rankId={}", userId, rankId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 明确回滚所有异常
    public boolean insertLove(Integer userId, Integer rankId) {
        // 1. 基础参数校验（避免空指针导致数据不一致）
        if (userId == null || rankId == null) {
            log.error("【收藏操作】参数为空，userId={}, rankId={}", userId, rankId);
            return false;
        }

        // 拼接Redis的key值
        String loveKey = String.format(Love_Key, userId, rankId);
        log.info("【收藏操作】开始，userId={}, rankId={}, RedisKey={}", userId, rankId, loveKey);

        // 2. 先同步数据库收藏状态到Redis（解决缓存和数据库不一致问题）
        syncLoveStatus(userId, rankId, loveKey);

        // 3. 核心：用Redis的setIfAbsent原子操作判断是否已收藏（杜绝竞态条件）
        Boolean isFirstLove = stringRedisTemplate.opsForValue().setIfAbsent(loveKey, "1", 24, TimeUnit.HOURS);
        // 防御性判断：Redis连接失败时直接返回
        if (isFirstLove == null) {
            log.error("【收藏操作】Redis连接失败，无法判断收藏状态");
            return false;
        }

        // ========== 分支1：已收藏（执行取消收藏） ==========
        if (Boolean.FALSE.equals(isFirstLove)) {
            log.info("【取消收藏】Redis已存在记录，开始删除数据库+Redis记录");
            try {
                // 调用Mapper删除（用@Param注解后参数顺序不再敏感，优先保证userId/rankId传对）
                int deleteResult = userPublishMapper.deleteLove(userId, rankId);
                log.info("【取消收藏】数据库删除影响行数：{}", deleteResult);

                // 数据库删除成功才删Redis，失败则删除Redis并抛异常
                if (deleteResult > 0) {
                    boolean deleteSuccess = stringRedisTemplate.delete(loveKey);
                    if (!deleteSuccess) {
                        log.warn("【取消收藏】第一次删除Redis Key失败，重试一次！Key={}", loveKey);
                        deleteSuccess = stringRedisTemplate.delete(loveKey);
                    }
                    log.info("【取消收藏】Redis Key删除结果：{}，Key={}", deleteSuccess, loveKey);
                    return deleteSuccess;
                } else {
                    log.error("【取消收藏】数据库删除失败，userId={}, rankId={}", userId, rankId);
                    // 数据库无记录，强制删除Redis避免缓存残留
                    stringRedisTemplate.delete(loveKey);
                    throw new RuntimeException("取消收藏：数据库无对应记录");
                }
            } catch (Exception e) {
                log.error("【取消收藏】执行失败", e);
                stringRedisTemplate.delete(loveKey); // 异常时删除Redis
                throw e; // 抛异常触发事务回滚
            }
        }

        // ========== 分支2：未收藏（执行新增收藏） ==========
        try {
            // 校验榜单是否存在
            PersonalRank l = userPublishMapper.selectRankById(rankId);
            if (l == null) {
                log.error("【收藏操作】榜单不存在，rankId={}", rankId);
                stringRedisTemplate.delete(loveKey); // 删除Redis占位key
                return false;
            }

            // 插入数据库（关键：确保userId/rankId参数顺序和Mapper的@Param匹配）
            int row1 = userPublishMapper.insertLove(userId, rankId);
            log.info("【收藏操作】数据库插入影响行数：{}", row1);

            // 数据库插入成功才保留Redis记录，失败则回滚
            if (row1 > 0) {
                log.info("【收藏成功】Redis已记录，userId={}, rankId={}", userId, rankId);
                return true;
            } else {
                log.error("【收藏失败】数据库插入无影响行数，userId={}, rankId={}", userId, rankId);
                stringRedisTemplate.delete(loveKey); // 删除Redis占位key
                throw new RuntimeException("收藏失败：数据库插入失败");
            }
        } catch (Exception e) {
            log.error("【收藏操作】执行失败", e);
            stringRedisTemplate.delete(loveKey); // 异常时删除Redis
            throw e; // 抛异常触发事务回滚
        }
    }

    /**
     * 同步数据库收藏状态到Redis（解决缓存和数据库不一致）
     */
    private void syncLoveStatus(Integer userId, Integer rankId, String loveKey) {
        // 先在Mapper中新增countLove方法（和点赞的countVote逻辑一致）
        Integer count = userPublishMapper.countLove(userId, rankId);
        if (count > 0 && !stringRedisTemplate.hasKey(loveKey)) {
            // 数据库有收藏记录，Redis无 → 同步到Redis
            stringRedisTemplate.opsForValue().set(loveKey, "1");
            log.info("【收藏缓存同步】数据库有记录，Redis同步成功，userId={}, rankId={}", userId, rankId);
        } else if (count == 0 && stringRedisTemplate.hasKey(loveKey)) {
            // 数据库无收藏记录，Redis有 → 删除Redis
            stringRedisTemplate.delete(loveKey);
            log.info("【收藏缓存同步】数据库无记录，Redis删除成功，userId={}, rankId={}", userId, rankId);
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
    // 可以在页面加载获取榜单信息时，或者在投票前调用这个方法
    public void syncVoteStatusFromDBToRedis(Integer userId, Integer rankId) {
        String voteKey = String.format(Vote_Key, userId, rankId);
        // 检查数据库中是否有记录
        VoteRecord record = userPublishMapper.countVote(userId, rankId); // 你需要实现这个查询方法
        if (record != null && !stringRedisTemplate.hasKey(voteKey)) {
            // 如果数据库有记录，但Redis没有，就同步到Redis
            stringRedisTemplate.opsForValue().set(voteKey, "1");
            log.info("【缓存同步】从数据库同步投票状态到Redis，userId={}, rankId={}", userId, rankId);
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
