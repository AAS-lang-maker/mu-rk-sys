local key = KEYS[1]
local member = ARGV[1]
local score = ARGV[2]

local oldRank = redis.call('ZREVRANK', key, member)

redis.call('ZADD', key, score, member)

local newRank = redis.call('ZREVRANK', key, member)

local topMemberInfo = redis.call('ZREVRANGE', key, 0, 0, 'WITHSCORES')

return { oldRank, newRank, topMemberInfo[1] }