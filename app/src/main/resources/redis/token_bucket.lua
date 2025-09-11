-- KEYS[1] = bucket key
-- ARGV[1] = capacity
-- ARGV[2] = refillPerSec
-- ARGV[3] = now(ms)

local key     = KEYS[1]
local cap     = tonumber(ARGV[1])
local rate    = tonumber(ARGV[2])
local nowMs   = tonumber(ARGV[3])

local data = redis.call('HMGET', key, 'tokens', 'ts')
local tokens = tonumber(data[1])
local ts     = tonumber(data[2])

if tokens == nil then
  tokens = cap
  ts = nowMs
else
  local elapsed = math.max(0, nowMs - ts) / 1000.0
  local refill  = elapsed * rate
  tokens = math.min(cap, tokens + refill)
  ts = nowMs
end

local allowed = 0
if tokens >= 1.0 then
  tokens = tokens - 1.0
  allowed = 1
end

redis.call('HMSET', key, 'tokens', tokens, 'ts', ts)
-- TTL을 대략 cap/rate 만큼 유지(활동 없으면 자연 소멸)
local ttl = math.ceil(cap / math.max(1, rate))
redis.call('EXPIRE', key, ttl)

-- 남은 정수 토큰, 다음 1개 보충까지 남은 ms
local remaining = math.floor(tokens)
local need = (tokens >= 1.0) and 0 or math.ceil((1.0 - tokens) / rate * 1000)
return {allowed, remaining, need}