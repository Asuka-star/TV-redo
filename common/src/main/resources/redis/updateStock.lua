local stock =redis.call('get',KEYS[1])
if not(stock) then
    return 1
end
local stockNum=tonumber(stock)
local newStock=stockNum+tonumber(ARGV[1])
if newStock<0 then
    return 2
end
local ttl = redis.call('ttl',KEYS[1]);
redis.call('set',KEYS[1],newStock)
if tonumber(ttl)>0 then
    redis.call('expire',KEYS[1],ttl)
end
return 0