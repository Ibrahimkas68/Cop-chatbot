-- Get the arguments
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local refill_interval_ms = tonumber(ARGV[3]) * 1000 -- Convert to milliseconds

-- Get the current state of the bucket
local bucket = redis.call('hgetall', key)
local last_refill_time_ms = 0
local tokens = capacity

if #bucket > 0 then
    for i = 1, #bucket, 2 do
        if bucket[i] == 'last_refill_time_ms' then
            last_refill_time_ms = tonumber(bucket[i+1])
        elseif bucket[i] == 'tokens' then
            tokens = tonumber(bucket[i+1])
        end
    end
end

-- Calculate the number of tokens to add
local now_ms = redis.call('time')[1] * 1000 + math.floor(redis.call('time')[2] / 1000)
local time_since_last_refill_ms = now_ms - last_refill_time_ms
local new_tokens = math.floor(time_since_last_refill_ms / refill_interval_ms) * refill_rate

if new_tokens > 0 then
    tokens = math.min(capacity, tokens + new_tokens)
    last_refill_time_ms = now_ms
end

-- Check if there are enough tokens
if tokens > 0 then
    tokens = tokens - 1
    redis.call('hset', key, 'tokens', tokens, 'last_refill_time_ms', last_refill_time_ms)
    redis.call('expire', key, refill_interval_ms * 2 / 1000) -- Expire the key (in seconds)
    return 1
else
    return 0
end
