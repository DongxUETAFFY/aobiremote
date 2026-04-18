package io.github.dongxuetaffy.aobihelper.common.metrics;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RequestMetricsFilter extends OncePerRequestFilter {
    private final RequestMetricsService requestMetricsService;

    public RequestMetricsFilter(RequestMetricsService requestMetricsService) {
        this.requestMetricsService = requestMetricsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        requestMetricsService.recordRequest();
        filterChain.doFilter(request, response);
    }
}
