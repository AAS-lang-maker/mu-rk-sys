package com.music.Service.impl;

import com.music.Mapper.MusicHotRankMapper;
import com.music.Service.HotRankService;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.Comment;
import com.music.pojo.PersonalRank;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HotRankServiceImpl implements HotRankService {
    //设置热门榜单Zset集合的键值
    //Redis核心写在Service层，配和Mapper层的Mysql查询联合完成热门榜单
    /*逻辑：    1.设置榜单排名的分数计算公式，自定义
               2.根据公式调用Redis，根据分数公式对分数进行排名并实时更新
                */
    private static final Logger log = LoggerFactory.getLogger(UserPublishServiceImpl.class);

    private static final String Hot_Rank_Key="music:rank:hot";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;



    private static final String Love_Key="love:user:%d:rank:%d";
    private static final String Vote_Key="vote:user:%d:rank:%d";

    @Autowired
    private final RedisTemplate<String, Object> redisTemplate;
    // 注入 RedisTemplat
    // 注入 WebSocket 发送工具
    private final SimpMessagingTemplate messagingTemplate;
    // 定义脚本对象
    private DefaultRedisScript<List> rankScript;
    @Autowired
    private MusicHotRankMapper musicHotRankMapper;
    @Override
    public Long caculateHotRank(Long rankId){
        Long love=musicHotRankMapper.CountLove(rankId);
        Long vote=musicHotRankMapper.CountVote(rankId);
        //防止空指针？
        love=love==null?0L:love;
        vote=vote==null?0L:vote;
        return love+2*vote;
    }

    // 构造函数注入
    public HotRankServiceImpl(RedisTemplate<String, Object> redisTemplate, SimpMessagingTemplate messagingTemplate) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
    }

    // 项目启动时加载 Lua 脚本
    @PostConstruct
    public void init() {
        rankScript = new DefaultRedisScript<>();
        rankScript.setResultType(List.class); // 返回值是列表

        try {
            // 读取 resources/lua/rank_update.lua
            ClassPathResource resource = new ClassPathResource("lua/rank_update.lua");
            InputStream inputStream = resource.getInputStream();
            String scriptContent = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            rankScript.setScriptText(scriptContent);
            System.out.println("✅ Lua 战报脚本加载成功！");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Lua 脚本加载失败！");
        }
    }


    /**
     * 更新热度并发送战报
     */
    @Override
    //第二步的逻辑包括：将新建的Zset集合中添加榜单各项数据+查询榜单排名
    public void updateHotRank(Long rankId) {
        if (rankId == null) {
            //直接抛出空指针异常
            throw new IllegalArgumentException("rankId不能为空");
        }/*
        System.out.println("===== 开始写入Redis =====");
        System.out.println("Key：" + Hot_Rank_Key);
        System.out.println("要写入的rankId：" + rankId);
        Long score=caculateHotRank(rankId);//此步相当于调用上方的自定义的计算分数的方法
        System.out.println("计算的热度值：" + score);
        //为什么toString？？？
        stringRedisTemplate.opsForZSet().add(Hot_Rank_Key,rankId.toString(),score);
        Set<String> afterWrite = stringRedisTemplate.opsForZSet().range(Hot_Rank_Key, 0, -1);
        System.out.println("写入后Redis中的数据：" + afterWrite);
        System.out.println("===== 写入Redis结束 =====");*/
        // ... 前面的空值判断 ...

        // 1. 计算热度分
        Long score = caculateHotRank(rankId);

           /* // 2. === 核心改动：执行 Lua 脚本 ===
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setLocation(new ClassPathResource("lua/rank_update.lua"));
            script.setResultType(List.class);

            List<Long> result = (List<Long>) stringRedisTemplate.execute(
                    script,
                    Arrays.asList(Hot_Rank_Key),
                    rankId.toString(),
                    String.valueOf(score)
            );

            // 3. === 处理战报逻辑 ===
            if (result != null) {
                Long oldRank = result.get(0);
                Long newRank = result.get(1);

                // 注意：Redis 排名从 0 开始，所以显示给用户要 +1
                int displayOld = oldRank == -1 ? 0 : oldRank.intValue() + 1;
                int displayNew = newRank.intValue() + 1;

                // 如果 旧排名 > 新排名（比如 5 -> 3），说明上升了
                // 或者 旧排名 == 0 (未上榜) -> 新排名 > 0 (上榜了)
                if ((oldRank > newRank && oldRank != -1) || (oldRank == -1 && newRank == 0)) {
                    String msg = "恭喜！你的热度飙升，排名从第 " + displayOld + " 名上升至第 " + displayNew + " 名！";
                    // TODO: 把这个 msg 返回给前端，或者存入战报表
                    System.out.println("🔥 触发战报：" + msg);
                }}*/
        // 2. 执行 Lua 脚本
        // KEYS[1] = "rank:hot"
        // ARGV[1] = rankId
        // ARGV[2] = incrementScore
        List<Object> result = (List<Object>) redisTemplate.execute(
                rankScript,
                Collections.singletonList("rank:hot"),
                rankId.toString(),
                String.valueOf(score)
        );

        // 3. 解析 Lua 返回的结果
        if (result != null && result.size() >= 3) {
            Long oldRank = (Long) result.get(0);
            Long newRank = (Long) result.get(1);
            String currentTopUserId = (String) result.get(2); // 拿到当前榜首 ID

            // ==========================================
            // 逻辑一：判断榜首是否更换 -> 全服广播
            // ==========================================
            // 从 Redis 获取“上一次记录的榜首”
            String lastTopUserId = (String) redisTemplate.opsForValue().get("rank:last_top_user");

            // 如果榜首变了（或者这是第一次）
            if (lastTopUserId == null || !lastTopUserId.equals(currentTopUserId)) {
                String publicMsg = "🏆 榜单风云！恭喜用户 " + currentTopUserId + " 成为最热门的榜单！！！";

                // 广播给所有人 (/topic 是公共频道)
                messagingTemplate.convertAndSend("/topic/public-news", publicMsg);

                // 更新 Redis 里的记录
                redisTemplate.opsForValue().set("rank:last_top_user", currentTopUserId);
                System.out.println("📢 全服广播：" + publicMsg);
            }

            // ==========================================
            // 逻辑二：判断个人排名上升 -> 私信创建者
            // ==========================================
            String personalMsg = null;

            // 首次上榜
            if (oldRank == -1) {
                personalMsg = "🎉 恭喜！你的榜单首次上榜，排名第 " + (newRank + 1) + "！";
            }
            // 排名上升 (注意：Redis 排名 0 是第一名，数值越小越强)
            else if (newRank < oldRank) {
                int diff = oldRank.intValue() - newRank.intValue();
                personalMsg = "🚀 排名上升！你的榜单提升了 " + diff + " 位，当前第 " + (newRank + 1) + "！";
            }

            // 假设你有一个榜单实体类 RankEntity
            PersonalRank personalRank = musicHotRankMapper.selectRankById(Math.toIntExact(rankId));
            Long authorId = personalRank.getUserId().longValue(); // 拿到真正的作者 ID
            // 发送私信 (/user 是个人频道)
            if (personalMsg != null) {
                // 发送给 rankId 这个用户
                messagingTemplate.convertAndSendToUser(
                        authorId.toString(),
                        "/queue/battle-report",
                        personalMsg
                );
                System.out.println("🔒 私信发送给用户 " + authorId + ": " + personalMsg);
            }
        }
    }
    @Override
    public Set<String> getHotRankId(int start,int end) {
        if(end<=0||start<0){
            return Collections.emptySet();//妙SOS,返回空集合
        }
         return stringRedisTemplate.opsForZSet().reverseRange(Hot_Rank_Key,start,end);
    }

    @Override
    public List<MyRankWithSong> listById(List<Long> rankId) {
        return musicHotRankMapper.listByIds(rankId);
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

            int deleteresult=musicHotRankMapper.deleteVote(userId,rankId);
            log.info("【取消投票】数据库影响行数：{}", deleteresult);
            return deleteresult>0;

        }
        PersonalRank v=musicHotRankMapper.selectRankById(rankId);
        if(v==null){
            return false;
        }
        int rows=musicHotRankMapper.insertVote(rankId,userId);
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
            int result=musicHotRankMapper.deleteLove(userId,rankId);
            log.info("【取消收藏】Redis记录已删除，userId={}, rankId={}", userId, rankId);
            stringRedisTemplate.delete(lovekey);
            log.info("【取消收藏】mysql记录已删除，userId={}, rankId={}", userId, rankId);
            return result>0;
        }
        PersonalRank l=musicHotRankMapper.selectRankById(rankId);
        if(l==null){
            return false;
        }
        int row1=musicHotRankMapper.insertLove(userId,rankId);
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
    @Transactional
    public boolean deleteVote(Integer rankId, Integer userId) {
        // 原有逻辑
        PersonalRank personalRank = musicHotRankMapper.selectRankById(rankId);
        if (personalRank == null) {
            // 新增日志：打印榜单不存在的提示
            log.warn("【取消点赞】榜单不存在！rankId={}, userId={}", rankId, userId);
            return false;
        }

        // 新增日志：打印要删除的条件
        log.info("【取消点赞】准备删除点赞记录：rankId={}, userId={}", rankId, userId);
        int result =musicHotRankMapper.deleteVote(rankId, userId);

        // 新增日志：打印删除结果（核心！看删了多少行）
        log.info("【取消点赞】删除操作完成，影响行数：{}", result);

        return result > 0;
    }

    // 你的deleteLove方法（同理加日志）
    @Override
    @Transactional
    public boolean deleteLove(Integer rankId, Integer userId) {
        PersonalRank personalRank = musicHotRankMapper.selectRankById(rankId);
        if (personalRank == null) {
            log.warn("【取消收藏】榜单不存在！rankId={}, userId={}", rankId, userId);
            return false;
        }

        log.info("【取消收藏】准备删除收藏记录：rankId={}, userId={}", rankId, userId);
        int result = musicHotRankMapper.deleteLove(rankId, userId);
        log.info("【取消收藏】删除操作完成，影响行数：{}", result);

        return result > 0;
    }

    @Override
    public List<CommentVo> selectComment(Integer rankId, Integer userId) {
        List<CommentVo> list= musicHotRankMapper.selectComment(rankId,userId);
        System.out.println("查询出的评论数："+list.size());
        return list;
    }

    @Override
    @Transactional
    public boolean deleteComment(Integer comId,Integer userId) {
        Comment comment= musicHotRankMapper.selectCommentById(comId);
        if(comment==null){
            return false;
        }
        if(comment.getUserId()!=userId){
            throw new RuntimeException("只能删除自己的评论");
        }
        int result= musicHotRankMapper.updateComment(comId,1);
        return result>0;
    }

    @Override
    @Transactional
    public boolean insertLike(Integer userId,Integer comId) {
        Comment comment=musicHotRankMapper.selectCommentById(comId);
        if(comment==null||comment.getIdDelete()==1){
            return false;
        }
        int result=musicHotRankMapper.insertLike(userId,comId);
        return result>0;
    }

    @Override
    @Transactional
    public boolean deleteLike(Integer comId, Integer userId) {
        Comment comment=musicHotRankMapper.selectCommentById(comId);
        if(comment==null||comment.getIdDelete()==1){
            return false;
        }
        int result=musicHotRankMapper.deleteLike(comId,userId);
        return result>0;
    }
    //豆包更新之后变权威了
    //下方方法目的：刷新页面之后不会让统统一用户能重复投票，使用redis达到记录用户投票状态的目的
    private void sychorVoteToRedis(Integer rankId,Integer userId,String votekey){
        //sychronise翻译为同步，有点像锁但不是，这里本质是一个方法，将redis和mysql同步
        int x=musicHotRankMapper.countVote(rankId,userId);
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
        int y=musicHotRankMapper.countLove(rankId,userId);
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
