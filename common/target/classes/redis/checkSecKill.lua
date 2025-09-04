if tonumber(redis.call('get',KEYS[1])) <=0 then
    return 1
end
if redis.call('sismember',KEYS[2],ARGV[1]) ==1 then
    return 2
end
redis.call('decr',KEYS[1])
redis.call('sadd',KEYS[2],ARGV[1])
if tonumber(redis.call('ttl',KEYS[2])) ==-1 then
    redis.call('expire',KEYS[2],tonumber(ARGV[2]))
end
return 0