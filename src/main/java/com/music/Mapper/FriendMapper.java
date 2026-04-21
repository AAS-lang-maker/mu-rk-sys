package com.music.Mapper;

import com.music.pojo.UserFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendMapper {

    /**
     * 统计：互关好友中，收藏过指定榜单的人数
     * 优化点：直接利用 is_mutual 字段，避免自连接查询
     */
    @Select("""
            SELECT COUNT(DISTINCT lr.user_id)
            FROM user_follow uf
            JOIN love_record lr ON lr.user_id = uf.follow_id
            WHERE uf.user_id = #{userId}
              AND uf.is_mutual = 1  -- 假设 1 代表已互关
              AND lr.rank_id = #{rankId}
            """)
    Integer countMutualFriendLoveRank(@Param("userId") Integer userId, @Param("rankId") Integer rankId);

    /**
     * 列表：互关好友中，收藏过指定榜单的用户名
     */
    @Select("""
            SELECT u.username
            FROM user_follow uf
            JOIN love_record lr ON lr.user_id = uf.follow_id
            JOIN user u ON u.user_id = uf.follow_id
            WHERE uf.user_id = #{userId}
              AND uf.is_mutual = 1  -- 直接筛选互关状态
              AND lr.rank_id = #{rankId}
            GROUP BY u.username   -- 防止同一个好友多次收藏同一榜单导致重复
            ORDER BY u.username ASC
            LIMIT #{limit}
            """)
    List<String> listMutualFriendLoveRankUsernames(@Param("userId") Integer userId,
                                                   @Param("rankId") Integer rankId,
                                                   @Param("limit") Integer limit);

    @Select("SELECT * FROM user_follow WHERE user_id = #{currentUserId} AND follow_id = #{targetUserId}")
    UserFollow isFollowed(Integer currentUserId, Integer targetUserId);

    @Insert("INSERT INTO user_follow (user_id, follow_id, is_mutual, create_time) VALUES (#{currentUserId}, #{targetUserId}, #{isMutual}, #{createTime})")
    void add(UserFollow userFollow);

    @Delete("DELETE FROM user_follow WHERE user_id = #{currentUserId} AND follow_id = #{targetUserId}")
    void delete(@Param("currentUserId") Integer currentUserId, @Param("targetUserId") Integer targetUserId);

    /**
     * 获取我的粉丝列表
     */
    @Select("SELECT * FROM user_follow WHERE user_id = #{currentUserId}")
    List<UserFollow> getMyFriends(Integer currentUserId);

    @Select( "SELECT * FROM user_follow WHERE follow_id = #{currentUserId}")
    List<UserFollow> getMymaster(Integer currentUserId);
}

