-- rank_update.lua
-- 这里的 KEYS[1] 是榜单的 Key
-- 这里的 ARGV[1] 是用户 ID
-- 这里的 ARGV[2] 是增加的热度值

local key = KEYS[1]
local user_id = ARGV[1]
local score_increment = tonumber(ARGV[2])

-- 1. 获取点赞前的排名 (ZREVRANK 返回降序排名，0 代表第一名)
local old_rank = redis.call('ZREVRANK', key, user_id)

-- 2. 执行加分
local new_score = redis.call('ZINCRBY', key, score_increment, user_id)

-- 3. 获取点赞后的排名
local new_rank = redis.call('ZREVRANK', key, user_id)

-- 4. 处理新用户情况（如果之前没上榜，old_rank 是 nil/false）
if old_rank == false then
    old_rank = -1
end

-- === 新增逻辑：获取当前榜首 ===
-- ZREVRANGE key 0 0 表示只取第1名（从0开始）
-- 返回的是一个数组，比如 {"user_101"}
local top_one = redis.call('ZREVRANGE', key, 0, 0)
local current_top_user = ""

if top_one and #top_one > 0 then
    current_top_user = top_one[1]
end

-- 5. 返回结果：{旧排名, 新排名, 当前榜首ID}
-- 注意：这里不再返回 new_score 了，因为 Java 代码里只需要这三个数据
return {old_rank, new_rank, current_top_user}