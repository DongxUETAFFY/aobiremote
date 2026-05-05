package io.github.dongxuetaffy.aobihelper.auth.cache;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.auth.cache", name = "type", havingValue = "redis", matchIfMissing = true)
public class RedisAuthCacheService implements AuthCacheService {
    private static final String REGISTER_CODE_KEY_PREFIX = "auth:register-code:";
    private static final String REGISTER_CODE_INTERVAL_KEY_PREFIX = "auth:register-code-interval:";
    private static final String REGISTER_CODE_HOURLY_KEY_PREFIX = "auth:register-code-hourly:";
    private static final String RESET_PASSWORD_CODE_KEY_PREFIX = "auth:reset-password-code:";
    private static final String RESET_PASSWORD_INTERVAL_KEY_PREFIX = "auth:reset-password-interval:";
    private static final String RESET_PASSWORD_HOURLY_KEY_PREFIX = "auth:reset-password-hourly:";
    private static final String LOGIN_FAIL_EMAIL_KEY_PREFIX = "auth:login-fail-email:";
    private static final String LOGIN_FAIL_IP_KEY_PREFIX = "auth:login-fail-ip:";
    private static final String RATE_LIMIT_KEY_PREFIX = "auth:rate-limit:";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisAuthCacheService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void storeRegisterCode(String email, String code, Duration ttl) {
        stringRedisTemplate.opsForValue().set(registerCodeKey(email), code, ttl);
    }

    @Override
    public String getRegisterCode(String email) {
        return stringRedisTemplate.opsForValue().get(registerCodeKey(email));
    }

    @Override
    public void removeRegisterCode(String email) {
        stringRedisTemplate.delete(registerCodeKey(email));
    }

    @Override
    public boolean hasRegisterInterval(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(registerIntervalKey(email)));
    }

    @Override
    public void markRegisterInterval(String email, Duration ttl) {
        stringRedisTemplate.opsForValue().set(registerIntervalKey(email), "1", ttl);
    }

    @Override
    public int getRegisterHourlyCount(String email) {
        return getInteger(registerHourlyKey(email));
    }

    @Override
    public void incrementRegisterHourlyCount(String email, Duration ttl) {
        incrementWithTtl(registerHourlyKey(email), ttl);
    }

    @Override
    public void storeResetPasswordCode(String email, String code, Duration ttl) {
        stringRedisTemplate.opsForValue().set(resetPasswordCodeKey(email), code, ttl);
    }

    @Override
    public String getResetPasswordCode(String email) {
        return stringRedisTemplate.opsForValue().get(resetPasswordCodeKey(email));
    }

    @Override
    public void removeResetPasswordCode(String email) {
        stringRedisTemplate.delete(resetPasswordCodeKey(email));
    }

    @Override
    public boolean hasResetPasswordInterval(String email) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(resetPasswordIntervalKey(email)));
    }

    @Override
    public void markResetPasswordInterval(String email, Duration ttl) {
        stringRedisTemplate.opsForValue().set(resetPasswordIntervalKey(email), "1", ttl);
    }

    @Override
    public int getResetPasswordHourlyCount(String email) {
        return getInteger(resetPasswordHourlyKey(email));
    }

    @Override
    public void incrementResetPasswordHourlyCount(String email, Duration ttl) {
        incrementWithTtl(resetPasswordHourlyKey(email), ttl);
    }

    @Override
    public int getLoginFailEmailCount(String email) {
        return getInteger(loginFailEmailKey(email));
    }

    @Override
    public int getLoginFailIpCount(String ip) {
        return getInteger(loginFailIpKey(ip));
    }

    @Override
    public void incrementLoginFailEmailCount(String email, Duration ttl) {
        incrementWithTtl(loginFailEmailKey(email), ttl);
    }

    @Override
    public void incrementLoginFailIpCount(String ip, Duration ttl) {
        incrementWithTtl(loginFailIpKey(ip), ttl);
    }

    @Override
    public void clearLoginFailEmailCount(String email) {
        stringRedisTemplate.delete(loginFailEmailKey(email));
    }

    @Override
    public void clearLoginFailIpCount(String ip) {
        stringRedisTemplate.delete(loginFailIpKey(ip));
    }

    @Override
    public int incrementRateLimitCounter(String bucket, String subjectKey, Duration ttl) {
        Long count = stringRedisTemplate.opsForValue().increment(rateLimitKey(bucket, subjectKey));
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(rateLimitKey(bucket, subjectKey), ttl);
        }
        return count == null ? 0 : count.intValue();
    }

    private int getInteger(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    private void incrementWithTtl(String key, Duration ttl) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, ttl);
        }
    }

    private String registerCodeKey(String email) {
        return REGISTER_CODE_KEY_PREFIX + email;
    }

    private String registerIntervalKey(String email) {
        return REGISTER_CODE_INTERVAL_KEY_PREFIX + email;
    }

    private String registerHourlyKey(String email) {
        return REGISTER_CODE_HOURLY_KEY_PREFIX + email;
    }

    private String resetPasswordCodeKey(String email) {
        return RESET_PASSWORD_CODE_KEY_PREFIX + email;
    }

    private String resetPasswordIntervalKey(String email) {
        return RESET_PASSWORD_INTERVAL_KEY_PREFIX + email;
    }

    private String resetPasswordHourlyKey(String email) {
        return RESET_PASSWORD_HOURLY_KEY_PREFIX + email;
    }

    private String loginFailEmailKey(String email) {
        return LOGIN_FAIL_EMAIL_KEY_PREFIX + email;
    }

    private String loginFailIpKey(String ip) {
        return LOGIN_FAIL_IP_KEY_PREFIX + ip;
    }

    private String rateLimitKey(String bucket, String subjectKey) {
        return RATE_LIMIT_KEY_PREFIX + bucket + ":" + subjectKey;
    }
}
