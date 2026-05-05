package io.github.dongxuetaffy.aobihelper.auth.cache;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app.auth.cache", name = "type", havingValue = "memory")
public class LocalAuthCacheService implements AuthCacheService {
    private final Map<String, TimedEntry<String>> stringEntries = new ConcurrentHashMap<>();
    private final Map<String, TimedEntry<Integer>> countEntries = new ConcurrentHashMap<>();

    @Override
    public void storeRegisterCode(String email, String code, Duration ttl) {
        stringEntries.put(registerCodeKey(email), TimedEntry.of(code, ttl));
    }

    @Override
    public String getRegisterCode(String email) {
        return getString(registerCodeKey(email));
    }

    @Override
    public void removeRegisterCode(String email) {
        stringEntries.remove(registerCodeKey(email));
    }

    @Override
    public boolean hasRegisterInterval(String email) {
        return getString(registerIntervalKey(email)) != null;
    }

    @Override
    public void markRegisterInterval(String email, Duration ttl) {
        stringEntries.put(registerIntervalKey(email), TimedEntry.of("1", ttl));
    }

    @Override
    public int getRegisterHourlyCount(String email) {
        return getCount(registerHourlyKey(email));
    }

    @Override
    public void incrementRegisterHourlyCount(String email, Duration ttl) {
        increment(registerHourlyKey(email), ttl);
    }

    @Override
    public void storeResetPasswordCode(String email, String code, Duration ttl) {
        stringEntries.put(resetPasswordCodeKey(email), TimedEntry.of(code, ttl));
    }

    @Override
    public String getResetPasswordCode(String email) {
        return getString(resetPasswordCodeKey(email));
    }

    @Override
    public void removeResetPasswordCode(String email) {
        stringEntries.remove(resetPasswordCodeKey(email));
    }

    @Override
    public boolean hasResetPasswordInterval(String email) {
        return getString(resetPasswordIntervalKey(email)) != null;
    }

    @Override
    public void markResetPasswordInterval(String email, Duration ttl) {
        stringEntries.put(resetPasswordIntervalKey(email), TimedEntry.of("1", ttl));
    }

    @Override
    public int getResetPasswordHourlyCount(String email) {
        return getCount(resetPasswordHourlyKey(email));
    }

    @Override
    public void incrementResetPasswordHourlyCount(String email, Duration ttl) {
        increment(resetPasswordHourlyKey(email), ttl);
    }

    @Override
    public int getLoginFailEmailCount(String email) {
        return getCount(loginFailEmailKey(email));
    }

    @Override
    public int getLoginFailIpCount(String ip) {
        return getCount(loginFailIpKey(ip));
    }

    @Override
    public void incrementLoginFailEmailCount(String email, Duration ttl) {
        increment(loginFailEmailKey(email), ttl);
    }

    @Override
    public void incrementLoginFailIpCount(String ip, Duration ttl) {
        increment(loginFailIpKey(ip), ttl);
    }

    @Override
    public void clearLoginFailEmailCount(String email) {
        countEntries.remove(loginFailEmailKey(email));
    }

    @Override
    public void clearLoginFailIpCount(String ip) {
        countEntries.remove(loginFailIpKey(ip));
    }

    @Override
    public int incrementRateLimitCounter(String bucket, String subjectKey, Duration ttl) {
        return incrementAndGet(rateLimitKey(bucket, subjectKey), ttl);
    }

    private void increment(String key, Duration ttl) {
        TimedEntry<Integer> current = countEntries.get(key);
        if (current == null || current.isExpired()) {
            countEntries.put(key, TimedEntry.of(1, ttl));
            return;
        }
        countEntries.put(key, new TimedEntry<>(current.value() + 1, current.expiresAt()));
    }

    private int incrementAndGet(String key, Duration ttl) {
        TimedEntry<Integer> current = countEntries.get(key);
        if (current == null || current.isExpired()) {
            countEntries.put(key, TimedEntry.of(1, ttl));
            return 1;
        }
        int nextValue = current.value() + 1;
        countEntries.put(key, new TimedEntry<>(nextValue, current.expiresAt()));
        return nextValue;
    }

    private int getCount(String key) {
        TimedEntry<Integer> entry = countEntries.get(key);
        if (entry == null || entry.isExpired()) {
            countEntries.remove(key);
            return 0;
        }
        return entry.value();
    }

    private String getString(String key) {
        TimedEntry<String> entry = stringEntries.get(key);
        if (entry == null || entry.isExpired()) {
            stringEntries.remove(key);
            return null;
        }
        return entry.value();
    }

    private String registerCodeKey(String email) {
        return "register-code:" + email;
    }

    private String registerIntervalKey(String email) {
        return "register-interval:" + email;
    }

    private String registerHourlyKey(String email) {
        return "register-hourly:" + email;
    }

    private String resetPasswordCodeKey(String email) {
        return "reset-password-code:" + email;
    }

    private String resetPasswordIntervalKey(String email) {
        return "reset-password-interval:" + email;
    }

    private String resetPasswordHourlyKey(String email) {
        return "reset-password-hourly:" + email;
    }

    private String loginFailEmailKey(String email) {
        return "login-fail-email:" + email;
    }

    private String loginFailIpKey(String ip) {
        return "login-fail-ip:" + ip;
    }

    private String rateLimitKey(String bucket, String subjectKey) {
        return "rate-limit:" + bucket + ":" + subjectKey;
    }

    private record TimedEntry<T>(T value, LocalDateTime expiresAt) {
        static <T> TimedEntry<T> of(T value, Duration ttl) {
            return new TimedEntry<>(value, LocalDateTime.now().plus(ttl));
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
}
