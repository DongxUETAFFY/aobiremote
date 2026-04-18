package io.github.dongxuetaffy.aobihelper.admin.service;

import io.github.dongxuetaffy.aobihelper.admin.dto.AdminPublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminUserPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminOverviewVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPageResponseVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPublicPostVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminUserVO;

public interface AdminService {
    AdminOverviewVO getOverview();

    AdminPageResponseVO<AdminUserVO> pageUsers(AdminUserPageQuery query);

    AdminUserVO updateUserStatus(Long userId, String status);

    AdminPageResponseVO<AdminPublicPostVO> pagePublicPosts(AdminPublicPostPageQuery query);

    void deletePublicPost(Long postId);
}

