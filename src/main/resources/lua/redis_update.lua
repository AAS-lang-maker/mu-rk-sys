-- 1. 参数接收
-- KEYS[1]: 排行榜的 Key (Hot_Rank_Key)
-- ARGV[1]: 成员 ID (rankId.toString())
-- ARGV[2]: 最终计算好的分数 (finalScore - Long类型)
local key = KEYS[1]
local member = ARGV[1]
local score = ARGV[2]

-- 2. 获取旧排名（ZADD 之前）
local oldRank = redis.call('ZREVRANK', key, member)

-- 3. 更新分数
-- 使用传入的最终分数覆盖旧分数
redis.call('ZADD', key, score, member)

-- 4. 获取当前用户的新排名 (0-based)
-- ZREVRANK 返回的是从大到小排名的索引，第一名是 0
local newRank = redis.call('ZREVRANK', key, member)

-- 5. 获取当前排行榜第 1 名的信息
-- ZREVRANGE key 0 0 WITHSCORES 获取第 1 名及其分数
local topMemberInfo = redis.call('ZREVRANGE', key, 0, 0, 'WITHSCORES')

-- 6. 返回结果给 Java
-- 对应 Java 代码中的 result.get(0), result.get(1), result.get(2)
-- 返回: { 旧排名, 新排名, 榜首ID }
return { oldRank, newRank, topMemberInfo[1] }