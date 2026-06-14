package com.music.Service.impl;

import com.alibaba.fastjson.JSON;
import com.music.Config.NativeWebSocketServer;
import com.music.Mapper.MusicHotRankMapper;
import com.music.Service.HotRankService;
import com.music.dto.BattleReport;
import com.music.dto.CommentVo;
import com.music.dto.MyRankWithSong;
import com.music.pojo.Comment;
import com.music.pojo.PersonalRank;
import com.music.pojo.RankTagVO;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HotRankServiceImpl implements HotRankService {
    //设置热门榜单Zset集合的键值
    //Redis核心写在Service层，配和Mapper层的Mysql查询联合完成热门榜单
    /*逻辑：    1.设置榜单排名的分数计算公式，自定义
               2.根据公式调用Redis，根据分数公式对分数进行排名并实时更新
                */
    private static final Logger log = LoggerFactory.getLogger(UserPublishServiceImpl.class);

    private static final String Hot_Rank_Key="music:rank:vote";
    private static final String HOT_RANK_ZSET_KEY = "music:rank:vote";
    // 构造器注入（合并为一个构造函数，确保所有 final 字段都被初始化）
    public HotRankServiceImpl(RedisTemplate<String, Object> redisTemplate) {

        this.redisTemplate = redisTemplate;

    }

    private final RedisTemplate<String, Object> redisTemplate;

    private DefaultRedisScript<List> rankScript;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String Love_Key="love:user:%d:rank:%d";
    private static final String Vote_Key="vote:user:%d:rank:%d";
    private static final String Vote_Count_Key = "music:rank:vote:count:%d";
    private static final String Love_Count_Key = "music:rank:love:count:%d";

    @Autowired
    private MusicHotRankMapper musicHotRankMapper;

    @Override
    public Integer caculateHotRank(Integer rankId){
        Integer love=getLoveCount(rankId);
        Integer vote = getVoteCount(rankId);
        //防止空指针？
        love=love==null?0:love;
        vote=vote==null?0:vote;
        return love+2*vote;
    }

    // 项目启动时加载 Lua 脚本
    @PostConstruct
    public void init() {
        rankScript = new DefaultRedisScript<>();
        rankScript.setResultType(List.class); // 返回值类型：List

        try {
            // 1. 读取 Lua 脚本
            ClassPathResource resource = new ClassPathResource("lua/redis_update.lua");
            // 使用 try-with-resources 自动关闭流，防止资源泄露
            try (InputStream inputStream = resource.getInputStream()) {
                String scriptContent = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                rankScript.setScriptText(scriptContent);
                log.info("✅ Lua 战报脚本加载成功！长度: {} 字节", scriptContent.length());
            }
        } catch (IOException e) {
            // 2. 使用 log.error 记录异常堆栈，方便排查
            log.error("❌ Lua 脚本加载失败！请检查 resources/lua/redis_update.lua 是否存在", e);
            // 3. 关键：加载失败直接抛出异常，阻止项目启动
            // 防止项目跑起来了，但一投票就报 NullPointerException
            throw new RuntimeException("Lua 脚本初始化失败", e);
        }
    }

    // 1. 加上 @Async，确保不阻塞主线程
    @Async("rankTask")
    @Override
    public void updateHotRank(Integer rankId) {
        if (rankId == null) {
            throw new IllegalArgumentException("rankId不能为空");
        }
        log.info("启用这个方法 ，开始更新热度");


    /*    Integer targetScore = caculateHotRank(rankId);
        Double currentScore = stringRedisTemplate.opsForZSet().score(Hot_Rank_Key, rankId.toString());
        int oldScore = currentScore == null ? 0 : currentScore.intValue();
        int scoreDelta = targetScore - oldScore;别算了会覆盖 次次新上榜 你撑得住？
        */


        //小兔子乖乖 把门开开  爱情像纠结的毛线 ~最后还是失眠 别管我了☺️ ε=(´ο｀*)))唉 现在是2026年 4月25日 00:02:41
        //当世界崩塌  我们 still here
        //发牢骚来了 verdurous mountain 最好听的一集 谁会不喜欢在深夜听上一首纯音乐呢

        Integer increment = caculateHotRank(rankId);
        if (increment <= 0) {
            log.warn("增量小于等于0，不执行更新");
            return;
        }

        long timestamp = System.currentTimeMillis()/1000;

        double currentScore = stringRedisTemplate.opsForZSet().score(Hot_Rank_Key, rankId.toString());

        final long BASE = 10000000000L;

        long oldScoreValue = currentScore==0? 0L : (long) currentScore;

        long oldRankScore = oldScoreValue / BASE;

        long newRankScore = oldRankScore + increment.longValue();


        long finalScore = newRankScore * BASE + timestamp;

        String scoreStr = String.valueOf(finalScore);

        System.out.println(">>> 最终传给 Redis 的分数: " + scoreStr);
        System.out.println("最终计算出的 Score: " + finalScore);

        List<Object> result = null;
        try {
            result = (List<Object>) stringRedisTemplate.execute(
                    rankScript,
                    Collections.singletonList(Hot_Rank_Key),
                            rankId.toString(),
                            scoreStr

                    );
        } catch (Exception e) {
            log.error("执行 Lua 脚本出错：{}", e.getMessage(),e);
            e.printStackTrace();
        }

        if (result != null && result.size() >= 3) {
            Number oldRankNum = (Number) result.get(0);
            Number newRankNum = (Number) result.get(1);

            Long oldRank = oldRankNum == null ? -1L : oldRankNum.longValue();

            Long newRank = newRankNum == null ? -1L : newRankNum.longValue();

            log.info("✅ 热度更新完成，rankId={}, oldRank={}, newRank={}", rankId, oldRank, newRank);
            String currentTopRankIdStr = String.valueOf(result.get(2));

                        // 逻辑一：全服广播 (榜首更换) -> 用原生 WebSocket
            if (newRank == 0) {
                String lastTopRankId = (String) redisTemplate.opsForValue().get("rank:last_top_user");

                if (lastTopRankId == null ||
                        !lastTopRankId.equals(currentTopRankIdStr)) {

                    PersonalRank topRank = musicHotRankMapper.selectRankById(Integer.valueOf(currentTopRankIdStr));
                    String displayName = (topRank != null && topRank.getRankName() != null)
                            ? topRank.getRankName() : "榜单#" + currentTopRankIdStr;

                    BattleReport publicReport = new BattleReport(
                            "SUCCESS",
                            "🏆 榜单风云",
                            "恭喜 **" + displayName + "** 登顶热门榜首！"
                    );

                    String jsonPublicMsg = JSON.toJSONString(publicReport);


                    redisTemplate.opsForValue().set("rank:last_top_user", currentTopRankIdStr);


                    NativeWebSocketServer.sendMessageToAll(jsonPublicMsg);

                    log.info("📢 全服广播发送：{}", publicReport.getTitle());

                }
            }
//喵 关注塔菲喵
            String personalMsg = null;
            if (oldRank == -1) {
                personalMsg = "🎉 恭喜！你的榜单首次上榜，排名第 " + (newRank + 1) + "！";
            } else if (newRank < oldRank) {
                int diff = oldRank.intValue() - newRank.intValue();
                personalMsg = "🚀 排名上升！你的榜单提升了 " + diff + " 位，当前第 " + (newRank + 1) + "！";
            }

            if (personalMsg != null) {
                PersonalRank currentRank = musicHotRankMapper.selectRankById(Math.toIntExact(rankId));
                if (currentRank != null) {
                    Integer targetUserId = currentRank.getUserId();
                    if (targetUserId != null) {
                        // 【修改点 3】构造私信战报
                        BattleReport personalReport = new BattleReport(
                                "INFO",
                                "排名变动",
                                personalMsg
                        );

                        String jsonPersonalMsg = JSON.toJSONString(personalReport);

                        NativeWebSocketServer.sendToUser(targetUserId.toString(), jsonPersonalMsg);

                        log.info("🔒 私信发送给用户 {}: {}", targetUserId, personalMsg);
                    }
                }
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
    public boolean insertVote(Integer userId, Integer rankId) {
        String votekey = String.format(Vote_Key, userId, rankId);
        log.info("【点赞操作】开始，userId={}, rankId={}", userId, rankId);

        // 原子占位：避免并发下重复点赞（先占位成功才允许插库）
        Boolean isFirstVote = stringRedisTemplate.opsForValue().setIfAbsent(votekey, "1", 24, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(isFirstVote)) {
            // --- 分支一：点赞 ---
            PersonalRank v = musicHotRankMapper.selectRankById(rankId);
            if (v == null) {
                stringRedisTemplate.delete(votekey);
                return false;
            }

            try {
                // 写入 DB (利用数据库唯一索引防止重复)
                int rows = musicHotRankMapper.insertVote(rankId, userId);
                if (rows > 0) {
                    increaseVoteCount(rankId);
                    log.info("【投票成功】Redis已记录");
                    return true;
                }
                stringRedisTemplate.delete(votekey);
                return false;
            } catch (Exception e) {
                // 插库失败回滚 Redis 占位，避免脏状态
                stringRedisTemplate.delete(votekey);
                log.warn("【投票异常】可能是重复点赞导致主键冲突: {}", e.getMessage());
                return false;
            }
        }

        // --- 分支二：取消点赞 ---
        log.info("【取消点赞】Redis已存在记录，执行取消");
        int rows = musicHotRankMapper.deleteVote(userId, rankId);
        if (rows > 0) {
            stringRedisTemplate.delete(votekey);
            decreaseVoteCount(rankId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean insertLove(Integer userId,  Integer rankId) {
        //拼接Redis的key值
        String lovekey=String.format(Love_Key,userId,rankId);
        log.info("【收藏操作】开始，userId={}, rankId={}, RedisKey={}", userId, rankId, lovekey);
        // 先以数据库为准判断是否已收藏，避免 Redis key 过期后重复插入
        int existed = musicHotRankMapper.countLove(rankId, userId);
        if (existed > 0) {
            // DB已收藏 -> 本次视为取消收藏（toggle 语义不变）
            stringRedisTemplate.opsForValue().set(lovekey, "1", 24, TimeUnit.HOURS);
            int deletedRows = musicHotRankMapper.deleteLove(userId, rankId);
            if (deletedRows > 0) {
                stringRedisTemplate.delete(lovekey);
                decreaseLoveCount(rankId, deletedRows);
                log.info("【取消收藏】已删除{}条收藏记录，userId={}, rankId={}", deletedRows, userId, rankId);
                return true;
            }
            return false;
        }

        Boolean isFirstLove = stringRedisTemplate.opsForValue().setIfAbsent(lovekey, "1", 24, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(isFirstLove)) {
            // --- 分支一：收藏 ---
            PersonalRank l=musicHotRankMapper.selectRankById(rankId);
            if(l==null){
                stringRedisTemplate.delete(lovekey);
                return false;
            }
            int row1=musicHotRankMapper.insertLove(userId,rankId);
            if(row1>0){
                increaseLoveCount(rankId);
                log.info("【收藏成功】Redis已记录，userId={}, rankId={}", userId, rankId);
                return true;
            }
            stringRedisTemplate.delete(lovekey);
            return false;
        }

        // --- 分支二：取消收藏 ---
        log.info("【取消收藏】Redis已存在记录，准备删除数据库记录");
        int deletedRows=musicHotRankMapper.deleteLove(userId,rankId);
        if (deletedRows > 0) {
            stringRedisTemplate.delete(lovekey);
            decreaseLoveCount(rankId, deletedRows);
            log.info("【取消收藏】mysql和Redis记录已删除，deletedRows={}, userId={}, rankId={}", deletedRows, userId, rankId);
            return true;
        }
        return false;
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
    private static final String RANK_DETAIL_KEY_PREFIX = "rank:detail:"; // String：存歌曲详情JSON
    private static final long CACHE_EXPIRE_MINUTES = 30L;           // 缓存过期时间

    @Override
    public List<MyRankWithSong> getHotRankWithCache(List<Long> rankIds) {
        List<MyRankWithSong> finalList = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        //detail 先查redis再查数据库 不要直接查数据库 太慢了 ！！
        List<String> cachedJsonList = rankIds.stream()
                .map(id -> stringRedisTemplate.opsForValue().get(RANK_DETAIL_KEY_PREFIX + id))
                .toList();

        //解析缓存，并找出未命中的 ID
        for (int i = 0; i < cachedJsonList.size(); i++) {
            String json = cachedJsonList.get(i);
            if (json != null) {
                finalList.add(JSON.parseObject(json, MyRankWithSong.class));
            } else {
                missingIds.add(rankIds.get(i));
            }
        }

        // 如果有缓存未命中的数据，去数据库批量查询
        if (!missingIds.isEmpty()) {
            List<MyRankWithSong> dbList = musicHotRankMapper.listByIds(missingIds);

            // 将查到的数据回填到 Redis 缓存中
            for (MyRankWithSong song : dbList) {
                String key = RANK_DETAIL_KEY_PREFIX + song.getRankId();
                redisTemplate.opsForValue().set(key, JSON.toJSONString(song), CACHE_EXPIRE_MINUTES, java.util.concurrent.TimeUnit.MINUTES);
                finalList.add(song);
            }
        }

        // 根据 rankIds 的原始顺序，重新排列 finalList
        Map<Integer, MyRankWithSong> songMap = finalList.stream()
                .collect(Collectors.toMap(MyRankWithSong::getRankId, myrankwithsong -> myrankwithsong));

        return rankIds.stream()
                .map(id -> songMap.get(id.intValue()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Integer getVoteCount(Integer rankId) {
        String countKey = String.format(Vote_Count_Key, rankId);
        String voteCountStr = stringRedisTemplate.opsForValue().get(countKey);
        if (voteCountStr != null) {
            try {
                return Integer.parseInt(voteCountStr);
            } catch (NumberFormatException e) {
                log.warn("【投票数缓存】格式异常，key={}, value={}", countKey, voteCountStr);
            }
        }
        Integer dbCount = musicHotRankMapper.CountVote(rankId);
        int safeCount = dbCount == null ? 0 : dbCount;
        stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));
        return safeCount;
    }

    private void increaseVoteCount(Integer rankId) {
        String countKey = String.format(Vote_Count_Key, rankId);
        String value = stringRedisTemplate.opsForValue().get(countKey);

        if (value == null) {
            Integer dbCount = musicHotRankMapper.CountVote(rankId);
            int safeCount = dbCount == null ? 0 : dbCount;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));


            stringRedisTemplate.opsForZSet().add(HOT_RANK_ZSET_KEY, rankId.toString(), safeCount);
            return;
        }

        // 计数 +1
        stringRedisTemplate.opsForValue().increment(countKey);

        // ZSet 分数 +1
        stringRedisTemplate.opsForZSet().incrementScore(HOT_RANK_ZSET_KEY, rankId.toString(), 1);
    }

    // 修改 decrease 方法
    private void decreaseVoteCount(Integer rankId) {
        String countKey = String.format(Vote_Count_Key, rankId);
        String value = stringRedisTemplate.opsForValue().get(countKey);

        if (value == null) {
            Integer dbCount = musicHotRankMapper.CountVote(rankId);
            int safeCount = dbCount == null ? 0 : dbCount;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));

            // 同步 ZSet 初始分数
            stringRedisTemplate.opsForZSet().add(HOT_RANK_ZSET_KEY, rankId.toString(), safeCount);
            return;
        }

        Long newValue = stringRedisTemplate.opsForValue().decrement(countKey);

        if (newValue != null && newValue < 0) {
            stringRedisTemplate.opsForValue().set(countKey, "0");
            // ZSet 分数修正为 0
            stringRedisTemplate.opsForZSet().add(HOT_RANK_ZSET_KEY, rankId.toString(), 0);
        } else {
            // ZSet 分数 -1
            stringRedisTemplate.opsForZSet().incrementScore(HOT_RANK_ZSET_KEY, rankId.toString(), -1);
        }
    }

    private Integer getLoveCount(Integer rankId) {
        String countKey = String.format(Love_Count_Key, rankId);
        String loveCountStr = stringRedisTemplate.opsForValue().get(countKey);
        if (loveCountStr != null) {
            try {
                return Integer.parseInt(loveCountStr);
            } catch (NumberFormatException e) {
                log.warn("【收藏数缓存】格式异常，key={}, value={}", countKey, loveCountStr);
            }
        }
        Integer dbCount = musicHotRankMapper.CountLove(rankId);
        int safeCount = dbCount == null ? 0 : dbCount;
        stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));
        return safeCount;
    }

    private void increaseLoveCount(Integer rankId) {
        String countKey = String.format(Love_Count_Key, rankId);
        String value = stringRedisTemplate.opsForValue().get(countKey);
        if (value == null) {
            Integer dbCount = musicHotRankMapper.CountLove(rankId);
            int safeCount = dbCount == null ? 0 : dbCount;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));
            return;
        }
        stringRedisTemplate.opsForValue().increment(countKey);
    }

    private void decreaseLoveCount(Integer rankId, int delta) {
        int step = Math.max(1, delta);
        String countKey = String.format(Love_Count_Key, rankId);
        String value = stringRedisTemplate.opsForValue().get(countKey);
        if (value == null) {
            Integer dbCount = musicHotRankMapper.CountLove(rankId);
            int safeCount = dbCount == null ? 0 : dbCount;
            stringRedisTemplate.opsForValue().set(countKey, String.valueOf(safeCount));
            return;
        }
        Long newValue = stringRedisTemplate.opsForValue().increment(countKey, -step);
        if (newValue != null && newValue < 0) {
            stringRedisTemplate.opsForValue().set(countKey, "0");
        }
    }
}
