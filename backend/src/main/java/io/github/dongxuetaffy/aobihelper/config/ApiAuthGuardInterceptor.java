package io.github.dongxuetaffy.aobihelper.config;

import cn.dev33.satoken.stp.StpUtil;
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
public class ApiAuthGuardInterceptor implements HandlerInterceptor {
    private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<WhitelistRule> WHITELIST_RULES = List.of(
        WhitelistRule.of(HttpMethod.GET, "/api/health"),
        WhitelistRule.of(HttpMethod.POST, "/api/auth/send-register-code"),
        WhitelistRule.of(HttpMethod.POST, "/api/auth/send-reset-password-code"),
        WhitelistRule.of(HttpMethod.POST, "/api/auth/login"),
        WhitelistRule.of(HttpMethod.POST, "/api/auth/register"),
        WhitelistRule.of(HttpMethod.POST, "/api/auth/reset-password"),
        WhitelistRule.of(HttpMethod.GET, "/api/public-post/page"),
        WhitelistRule.of(HttpMethod.GET, "/api/public-post/*"),
        WhitelistRule.of(HttpMethod.GET, "/api/files/*/preview"),
        WhitelistRule.of(HttpMethod.POST, "/api/feedback")
    );

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = URL_PATH_HELPER.getPathWithinApplication(request);
        if (isWhitelisted(request.getMethod(), path)) {
            return true;
        }

        StpUtil.checkLogin();
        return true;
    }

    private boolean isWhitelisted(String method, String path) {
        return WHITELIST_RULES.stream()
            .anyMatch(rule -> rule.matches(method, path));
    }

    private record WhitelistRule(HttpMethod method, String pattern) {
        private static WhitelistRule of(HttpMethod method, String pattern) {
            return new WhitelistRule(method, pattern);
        }

        private boolean matches(String requestMethod, String requestPath) {
            return method.matches(requestMethod) && PATH_MATCHER.match(pattern, requestPath);
        }
    }
}
