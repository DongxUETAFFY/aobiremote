package io.github.dongxuetaffy.aobihelper.admin.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminPublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminUpdateUserStatusRequest;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminUserPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.service.AdminService;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminOverviewVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPageResponseVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPublicPostVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminUserVO;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminOverviewVO> overview() {
        return ApiResponse.success(adminService.getOverview());
    }

    @GetMapping("/users")
    public ApiResponse<AdminPageResponseVO<AdminUserVO>> users(@ModelAttribute AdminUserPageQuery query) {
        return ApiResponse.success(adminService.pageUsers(query));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<AdminUserVO> updateUserStatus(
        @PathVariable Long id,
        @Valid @RequestBody AdminUpdateUserStatusRequest request
    ) {
        return ApiResponse.success("User status updated", adminService.updateUserStatus(id, request.getStatus()));
    }

    @GetMapping("/public-posts")
    public ApiResponse<AdminPageResponseVO<AdminPublicPostVO>> publicPosts(@ModelAttribute AdminPublicPostPageQuery query) {
        return ApiResponse.success(adminService.pagePublicPosts(query));
    }

    @DeleteMapping("/public-posts/{id}")
    public ApiResponse<Void> deletePublicPost(@PathVariable Long id) {
        adminService.deletePublicPost(id);
        return ApiResponse.success("Public post deleted", null);
    }
}

