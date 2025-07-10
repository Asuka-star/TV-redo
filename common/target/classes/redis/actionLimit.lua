local key = KEYS[1]
local maxCount = tonumber(ARGV[1])
local windowSec = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local member = ARGV[4]
redis.call('ZREMRANGEBYSCORE', key, '-inf', now - windowSec)
local count = redis.call('ZCARD', key)
if count < maxCount then
redis.call('ZADD', key, now, member)
redis.call('EXPIRE', key, windowSec)
return 1
end
return 0