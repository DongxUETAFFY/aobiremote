package io.github.dongxuetaffy.aobihelper.common.guard.service.impl;

import io.github.dongxuetaffy.aobihelper.auth.cache.AuthCacheService;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.guard.service.AnonymousPublicReadThrottleService;
import io.github.dongxuetaffy.aobihelper.config.PublicAccessProperties;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class AnonymousPublicReadThrottleServiceImpl implements AnonymousPublicReadThrottleService {
    private static final String TOO_FREQUENT_MESSAGE = "Public access is too frequent, please try again later";

    private final AuthCacheService authCacheService;
    private final PublicAccessProperties publicAccessProperties;

    public AnonymousPublicReadThrottleServiceImpl(
        AuthCacheService authCacheService,
        PublicAccessProperties publicAccessProperties
    ) {
        this.authCacheService = authCacheService;
        this.publicAccessProperties = publicAccessProperties;
    }

    @Override
    public void assertPublicPostPageAllowed(String clientIp) {
        assertAllowed("public-post-page", clientIp, publicAccessProperties.getPageLimit(), publicAccessProperties.getPageWindowSeconds());
    }

    @Override
    public void assertPublicPostDetailAllowed(String clientIp) {
        assertAllowed("public-post-detail", clientIp, publicAccessProperties.getDetailLimit(), publicAccessProperties.getDetailWindowSeconds());
    }

    @Override
    public void assertFilePreviewAllowed(String clientIp) {
        assertAllowed("file-preview", clientIp, publicAccessProperties.getPreviewLimit(), publicAccessProperties.getPreviewWindowSeconds());
    }

    private void assertAllowed(String bucket, String clientIp, int limit, int windowSeconds) {
        if (limit <= 0 || windowSeconds <= 0) {
            return;
        }
        int currentCount = authCacheService.incrementRateLimitCounter(bucket, normalizeClientIp(clientIp), Duration.ofSeconds(windowSeconds));
        if (currentCount > limit) {
            throw new BusinessException(BusinessCode.TOO_FREQUENT, TOO_FREQUENT_MESSAGE);
        }
    }

    private String normalizeClientIp(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return "unknown";
        }
        return clientIp.trim();
    }
}
