package io.github.dongxuetaffy.aobihelper.auth.cache;

import java.time.Duration;

public interface AuthCacheService {
    void storeRegisterCode(String email, String code, Duration ttl);

    String getRegisterCode(String email);

    void removeRegisterCode(String email);

    boolean hasRegisterInterval(String email);

    void markRegisterInterval(String email, Duration ttl);

    int getRegisterHourlyCount(String email);

    void incrementRegisterHourlyCount(String email, Duration ttl);

    void storeResetPasswordCode(String email, String code, Duration ttl);

    String getResetPasswordCode(String email);

    void removeResetPasswordCode(String email);

    boolean hasResetPasswordInterval(String email);

    void markResetPasswordInterval(String email, Duration ttl);

    int getResetPasswordHourlyCount(String email);

    void incrementResetPasswordHourlyCount(String email, Duration ttl);

    int getLoginFailEmailCount(String email);

    int getLoginFailIpCount(String ip);

    void incrementLoginFailEmailCount(String email, Duration ttl);

    void incrementLoginFailIpCount(String ip, Duration ttl);

    void clearLoginFailEmailCount(String email);

    void clearLoginFailIpCount(String ip);

    int incrementRateLimitCounter(String bucket, String subjectKey, Duration ttl);
}
