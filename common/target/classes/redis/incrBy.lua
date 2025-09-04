local res = redis.call('incrBy', KEYS[1], ARGV[1])
redis.call('expire', KEYS[1], ARGV[2])
return res
