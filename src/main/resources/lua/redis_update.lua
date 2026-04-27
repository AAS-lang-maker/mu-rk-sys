-- KEYS[1]: Key
-- ARGV[1]: Member
-- ARGV[2]: FinalScore (已经是纯数字字符串，如 "61777257158")

local key = KEYS[1]
local member = ARGV[1]
local scoreStr = ARGV[2]

-- 1. 更新分数 (Redis ZADD 接受字符串格式的数字)
redis.call('ZADD', key, scoreStr, member)

-- 2. 【核心修改】用字符串截取代替除法，避开精度坑
-- 我们的算法是：热度分 * 10000000000 + 时间戳(10位)
-- 所以，只要把字符串最后 10 位去掉，剩下的就是热度分！
local len = string.len(scoreStr)
local rankScore = 0

if len > 10 then
    -- 截取从第 1 位到 (总长度-10) 位
    rankScore = string.sub(scoreStr, 1, len - 10)
else
    -- 如果长度不足 10 位（极少见），说明热度分是 0
    rankScore = 0
end

-- 转成数字返回（可选）
rankScore = tonumber(rankScore)

-- 3. 获取排名
local rank = redis.call('ZREVRANK', key, member)
if rank then
    rank = rank + 1
end

return {rank, rankScore}