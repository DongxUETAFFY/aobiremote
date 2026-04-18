package io.github.dongxuetaffy.aobihelper.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.admin.service.AdminAccessService;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.config.AdminProperties;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AdminAccessServiceImpl implements AdminAccessService {
    private final UserAccountMapper userAccountMapper;
    private final AdminProperties adminProperties;

    public AdminAccessServiceImpl(UserAccountMapper userAccountMapper, AdminProperties adminProperties) {
        this.userAccountMapper = userAccountMapper;
        this.adminProperties = adminProperties;
    }

    @Override
    public UserAccount assertCurrentUserAdmin() {
        StpUtil.checkLogin();
        UserAccount userAccount = userAccountMapper.selectById(StpUtil.getLoginIdAsLong());
        if (userAccount == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }
        if (!isAdmin(userAccount)) {
            throw new BusinessException(BusinessCode.FORBIDDEN, "Admin permission required");
        }
        return userAccount;
    }

    @Override
    public boolean isAdmin(UserAccount userAccount) {
        if (userAccount == null || userAccount.getEmail() == null) {
            return false;
        }
        return normalizedAdminEmails().contains(userAccount.getEmail().trim().toLowerCase(Locale.ROOT));
    }

    private Set<String> normalizedAdminEmails() {
        return adminProperties.getEmails().stream()
            .filter(email -> email != null && !email.isBlank())
            .map(email -> email.trim().toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }
}

