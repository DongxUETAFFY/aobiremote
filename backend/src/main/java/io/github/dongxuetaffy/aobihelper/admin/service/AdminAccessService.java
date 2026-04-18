package io.github.dongxuetaffy.aobihelper.admin.service;

import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;

public interface AdminAccessService {
    UserAccount assertCurrentUserAdmin();

    boolean isAdmin(UserAccount userAccount);
}

