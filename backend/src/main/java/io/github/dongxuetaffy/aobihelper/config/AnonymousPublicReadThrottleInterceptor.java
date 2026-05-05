package io.github.dongxuetaffy.aobihelper.config;

import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.common.guard.service.AnonymousPublicReadThrottleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

@Component
public class AnonymousPublicReadThrottleInterceptor implements HandlerInterceptor {
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<ThrottleRule> THROTTLE_RULES = List.of(
        new ThrottleRule(HttpMethod.GET, "/api/public-post/page", ThrottleTarget.PUBLIC_POST_PAGE),
        new ThrottleRule(HttpMethod.GET, "/api/public-post/*", ThrottleTarget.PUBLIC_POST_DETAIL),
        new ThrottleRule(HttpMethod.GET, "/api/files/*/preview", ThrottleTarget.FILE_PREVIEW)
    );

    private final AnonymousPublicReadThrottleService anonymousPublicReadThrottleService;

    public AnonymousPublicReadThrottleInterceptor(AnonymousPublicReadThrottleService anonymousPublicReadThrottleService) {
        this.anonymousPublicReadThrottleService = anonymousPublicReadThrottleService;
    }

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || StpUtil.isLogin()) {
            return true;
        }

        String path = URL_PATH_HELPER.getPathWithinApplication(request);
        ThrottleRule matchedRule = findMatchedRule(request.getMethod(), path);
        if (matchedRule == null) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        switch (matchedRule.target()) {
            case PUBLIC_POST_PAGE -> anonymousPublicReadThrottleService.assertPublicPostPageAllowed(clientIp);
            case PUBLIC_POST_DETAIL -> anonymousPublicReadThrottleService.assertPublicPostDetailAllowed(clientIp);
            case FILE_PREVIEW -> anonymousPublicReadThrottleService.assertFilePreviewAllowed(clientIp);
        }
        return true;
    }

    private ThrottleRule findMatchedRule(String method, String path) {
        return THROTTLE_RULES.stream()
            .filter(rule -> rule.matches(method, path))
            .findFirst()
            .orElse(null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstIp = forwardedFor.split(",", 2)[0].trim();
            if (!firstIp.isBlank()) {
                return firstIp;
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private record ThrottleRule(HttpMethod method, String pattern, ThrottleTarget target) {
        private boolean matches(String requestMethod, String requestPath) {
            return method.matches(requestMethod) && PATH_MATCHER.match(pattern, requestPath);
        }
    }

    private enum ThrottleTarget {
        PUBLIC_POST_PAGE,
        PUBLIC_POST_DETAIL,
        FILE_PREVIEW
    }
}
