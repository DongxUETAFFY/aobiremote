package io.github.dongxuetaffy.aobihelper.common.guard.service;

public interface AnonymousPublicReadThrottleService {
    void assertPublicPostPageAllowed(String clientIp);

    void assertPublicPostDetailAllowed(String clientIp);

    void assertFilePreviewAllowed(String clientIp);
}
