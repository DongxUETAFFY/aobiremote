package io.github.dongxuetaffy.aobihelper.publiczone.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostCreateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostToggleUntrustedRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.dto.PublicPostUpdateRequest;
import io.github.dongxuetaffy.aobihelper.publiczone.service.PublicPostService;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostDetailVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostIdVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostPageResponseVO;
import io.github.dongxuetaffy.aobihelper.publiczone.vo.PublicPostToggleUntrustedVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public-post")
public class PublicZoneController {
    private final PublicPostService publicPostService;

    public PublicZoneController(PublicPostService publicPostService) {
        this.publicPostService = publicPostService;
    }

    @GetMapping("/page")
    public ApiResponse<PublicPostPageResponseVO> pagePublicPosts(@ModelAttribute PublicPostPageQuery query) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ApiResponse.success(publicPostService.pagePublicPosts(currentUserId, query));
    }

    @GetMapping("/{id}")
    public ApiResponse<PublicPostDetailVO> getPublicPostDetail(@PathVariable Long id) {
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        return ApiResponse.success(publicPostService.getPublicPostDetail(currentUserId, id));
    }

    @SaCheckLogin
    @PostMapping
    public ApiResponse<PublicPostIdVO> createPublicPost(@Valid @RequestBody PublicPostCreateRequest request) {
        return ApiResponse.success("Public post created", publicPostService.createPublicPost(StpUtil.getLoginIdAsLong(), request));
    }

    @SaCheckLogin
    @PutMapping("/{id}")
    public ApiResponse<PublicPostIdVO> updatePublicPost(
        @PathVariable Long id,
        @Valid @RequestBody PublicPostUpdateRequest request
    ) {
        return ApiResponse.success("Public post updated", publicPostService.updatePublicPost(StpUtil.getLoginIdAsLong(), id, request));
    }

    @SaCheckLogin
    @PostMapping("/{id}/toggle-untrusted")
    public ApiResponse<PublicPostToggleUntrustedVO> togglePublicPostUntrusted(
        @PathVariable Long id,
        @Valid @RequestBody PublicPostToggleUntrustedRequest request
    ) {
        return ApiResponse.success(
            "Public post untrusted state toggled",
            publicPostService.togglePublicPostUntrusted(StpUtil.getLoginIdAsLong(), id, request)
        );
    }

    @SaCheckLogin
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePublicPost(
        @PathVariable Long id,
        @RequestParam(required = false) String requestId
    ) {
        publicPostService.deletePublicPost(StpUtil.getLoginIdAsLong(), id, requestId);
        return ApiResponse.success("Public post deleted", null);
    }
}
