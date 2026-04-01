-- Redis 限流脚本 (滑动窗口算法)
-- KEYS[1]: 限流key
-- ARGV[1]: 最大次数
-- ARGV[2]: 时间窗口(秒)
-- ARGV[3]: 当前时间戳(毫秒)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2]) * 1000  -- 转换为毫秒
local current = tonumber(ARGV[3])

-- 清理过期记录
redis.call('ZREMRANGEBYSCORE', key, 0, current - window)

-- 获取当前窗口内的请求次数
local count = redis.call('ZCARD', key)

if count < limit then
    -- 未超过限制，添加当前请求记录
    redis.call('ZADD', key, current, current .. ':' .. math.random())
    redis.call('EXPIRE', key, ARGV[2])
    return 1
else
    -- 已超过限制
    return 0
end