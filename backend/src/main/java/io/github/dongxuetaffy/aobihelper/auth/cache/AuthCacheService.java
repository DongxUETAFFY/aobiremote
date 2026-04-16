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

    int getLoginFailEmailCount(String email);

    int getLoginFailIpCount(String ip);

    void incrementLoginFailEmailCount(String email, Duration ttl);

    void incrementLoginFailIpCount(String ip, Duration ttl);

    void clearLoginFailEmailCount(String email);

    void clearLoginFailIpCount(String ip);
}
