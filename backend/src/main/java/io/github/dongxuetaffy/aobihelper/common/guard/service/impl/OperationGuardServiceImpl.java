package io.github.dongxuetaffy.aobihelper.common.guard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.guard.entity.OperationGuard;
import io.github.dongxuetaffy.aobihelper.common.guard.mapper.OperationGuardMapper;
import io.github.dongxuetaffy.aobihelper.common.guard.service.OperationGuardService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationGuardServiceImpl implements OperationGuardService {
    private static final long MIN_INTERVAL_MILLIS = 1200L;
    private static final long WINDOW_MILLIS = 60_000L;
    private static final int WINDOW_LIMIT = 20;

    private final OperationGuardMapper operationGuardMapper;

    public OperationGuardServiceImpl(OperationGuardMapper operationGuardMapper) {
        this.operationGuardMapper = operationGuardMapper;
    }

    @Override
    @Transactional
    public void assertMutationAllowed(Long userId, String action, String requestId) {
        String normalizedRequestId = normalizeRequiredText(requestId, "Missing requestId");
        String guardKey = action + "_" + userId;
        LocalDateTime now = LocalDateTime.now();
        long nowMillis = System.currentTimeMillis();

        OperationGuard guard = operationGuardMapper.selectOne(
            new LambdaQueryWrapper<OperationGuard>()
                .eq(OperationGuard::getGuardKey, guardKey)
                .last("limit 1")
        );

        List<Long> recentTimes = guard == null
            ? new ArrayList<>()
            : filterRecentTimes(parseRecentTimes(guard.getRecentTimesJson()), nowMillis);

        if (guard != null && normalizedRequestId.equals(guard.getLastRequestId())) {
            throw new BusinessException(BusinessCode.DUPLICATE_SUBMIT, "Do not submit the same request repeatedly");
        }

        if (guard != null && guard.getLastSubmitAt() != null) {
            long millisSinceLastSubmit = Duration.between(guard.getLastSubmitAt(), now).toMillis();
            if (millisSinceLastSubmit < MIN_INTERVAL_MILLIS) {
                throw new BusinessException(BusinessCode.TOO_FREQUENT, "Operation is too frequent, please try again later");
            }
        }

        if (recentTimes.size() >= WINDOW_LIMIT) {
            throw new BusinessException(BusinessCode.TOO_FREQUENT, "Operation is too frequent, please try again later");
        }

        recentTimes.add(nowMillis);

        if (guard == null) {
            guard = new OperationGuard();
            guard.setUserId(userId);
            guard.setAction(action);
            guard.setGuardKey(guardKey);
            guard.setCreatedAt(now);
        }

        guard.setLastRequestId(normalizedRequestId);
        guard.setLastSubmitAt(now);
        guard.setRecentTimesJson(joinRecentTimes(recentTimes));
        guard.setUpdatedAt(now);

        if (guard.getId() == null) {
            operationGuardMapper.insert(guard);
        } else {
            operationGuardMapper.updateById(guard);
        }
    }

    private String normalizeRequiredText(String value, String message) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, message);
        }
        return normalized;
    }

    private List<Long> parseRecentTimes(String recentTimesJson) {
        List<Long> values = new ArrayList<>();
        if (recentTimesJson == null || recentTimesJson.isBlank()) {
            return values;
        }
        String[] parts = recentTimesJson.split(",");
        for (String part : parts) {
            String normalized = part.trim();
            if (normalized.isEmpty()) {
                continue;
            }
            try {
                values.add(Long.parseLong(normalized));
            } catch (NumberFormatException ignored) {
                // Ignore invalid historical data and continue with valid entries.
            }
        }
        return values;
    }

    private List<Long> filterRecentTimes(List<Long> values, long nowMillis) {
        List<Long> filtered = new ArrayList<>();
        for (Long value : values) {
            if (value != null && nowMillis - value < WINDOW_MILLIS) {
                filtered.add(value);
            }
        }
        return filtered;
    }

    private String joinRecentTimes(List<Long> values) {
        StringBuilder builder = new StringBuilder();
        for (Long value : values) {
            if (value == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
