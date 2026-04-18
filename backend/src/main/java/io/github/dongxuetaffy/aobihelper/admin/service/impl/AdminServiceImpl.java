package io.github.dongxuetaffy.aobihelper.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminPublicPostPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.dto.AdminUserPageQuery;
import io.github.dongxuetaffy.aobihelper.admin.service.AdminAccessService;
import io.github.dongxuetaffy.aobihelper.admin.service.AdminService;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminOverviewVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPageResponseVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminPublicPostVO;
import io.github.dongxuetaffy.aobihelper.admin.vo.AdminUserVO;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.common.metrics.RequestMetricsService;
import io.github.dongxuetaffy.aobihelper.config.FileProperties;
import io.github.dongxuetaffy.aobihelper.file.entity.FileAsset;
import io.github.dongxuetaffy.aobihelper.file.mapper.FileAssetMapper;
import io.github.dongxuetaffy.aobihelper.inventory.entity.InventoryItem;
import io.github.dongxuetaffy.aobihelper.inventory.mapper.InventoryItemMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPost;
import io.github.dongxuetaffy.aobihelper.publiczone.entity.PublicPostFlag;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostFlagMapper;
import io.github.dongxuetaffy.aobihelper.publiczone.mapper.PublicPostMapper;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
    private static final String USER_STATUS_ACTIVE = "active";
    private static final String USER_STATUS_DISABLED = "disabled";
    private static final long DEFAULT_PAGE_NO = 1L;
    private static final long DEFAULT_PAGE_SIZE = 30L;
    private static final long MAX_PAGE_SIZE = 50L;

    private final AdminAccessService adminAccessService;
    private final UserAccountMapper userAccountMapper;
    private final InventoryItemMapper inventoryItemMapper;
    private final PublicPostMapper publicPostMapper;
    private final PublicPostFlagMapper publicPostFlagMapper;
    private final FileAssetMapper fileAssetMapper;
    private final FileProperties fileProperties;
    private final RequestMetricsService requestMetricsService;

    public AdminServiceImpl(
        AdminAccessService adminAccessService,
        UserAccountMapper userAccountMapper,
        InventoryItemMapper inventoryItemMapper,
        PublicPostMapper publicPostMapper,
        PublicPostFlagMapper publicPostFlagMapper,
        FileAssetMapper fileAssetMapper,
        FileProperties fileProperties,
        RequestMetricsService requestMetricsService
    ) {
        this.adminAccessService = adminAccessService;
        this.userAccountMapper = userAccountMapper;
        this.inventoryItemMapper = inventoryItemMapper;
        this.publicPostMapper = publicPostMapper;
        this.publicPostFlagMapper = publicPostFlagMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.fileProperties = fileProperties;
        this.requestMetricsService = requestMetricsService;
    }

    @Override
    public AdminOverviewVO getOverview() {
        adminAccessService.assertCurrentUserAdmin();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long totalFiles = fileAssetMapper.selectCount(null);
        StorageStats storageStats = resolveStorageStats();
        return new AdminOverviewVO(
            userAccountMapper.selectCount(null),
            userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getStatus, USER_STATUS_ACTIVE)),
            userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>().eq(UserAccount::getStatus, USER_STATUS_DISABLED)),
            userAccountMapper.selectCount(new LambdaQueryWrapper<UserAccount>().ge(UserAccount::getCreatedAt, todayStart)),
            inventoryItemMapper.selectCount(null),
            publicPostMapper.selectCount(null),
            publicPostMapper.selectCount(new LambdaQueryWrapper<PublicPost>().ge(PublicPost::getCreatedAt, todayStart)),
            totalFiles,
            totalFiles,
            sumImageBytes(),
            storageStats.totalBytes(),
            storageStats.usableBytes(),
            sumUntrustedCount(),
            requestMetricsService.getRecentQps()
        );
    }

    @Override
    public AdminPageResponseVO<AdminUserVO> pageUsers(AdminUserPageQuery query) {
        adminAccessService.assertCurrentUserAdmin();
        Page<UserAccount> page = new Page<>(normalizePageNo(query.getPageNo()), normalizePageSize(query.getPageSize()));
        LambdaQueryWrapper<UserAccount> wrapper = new LambdaQueryWrapper<>();
        String keyword = normalizeNullableText(query.getKeyword());
        String status = normalizeNullableText(query.getStatus());
        if (keyword != null) {
            wrapper.and(item -> item.like(UserAccount::getEmail, keyword).or().like(UserAccount::getNickname, keyword));
        }
        if (status != null) {
            validateUserStatus(status);
            wrapper.eq(UserAccount::getStatus, status);
        }
        wrapper.orderByDesc(UserAccount::getCreatedAt);

        Page<UserAccount> result = userAccountMapper.selectPage(page, wrapper);
        return new AdminPageResponseVO<>(
            result.getRecords().stream().map(this::toUserVO).toList(),
            result.getCurrent(),
            result.getSize(),
            result.getTotal(),
            result.getPages()
        );
    }

    @Override
    @Transactional
    public AdminUserVO updateUserStatus(Long userId, String status) {
        UserAccount admin = adminAccessService.assertCurrentUserAdmin();
        String normalizedStatus = normalizeRequiredText(status);
        validateUserStatus(normalizedStatus);
        if (admin.getId().equals(userId) && USER_STATUS_DISABLED.equals(normalizedStatus)) {
            throw new BusinessException(BusinessCode.STATE_INVALID, "Cannot disable current admin account");
        }

        UserAccount user = userAccountMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }
        user.setStatus(normalizedStatus);
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(user);
        if (USER_STATUS_DISABLED.equals(normalizedStatus)) {
            StpUtil.logout(userId);
        }
        return toUserVO(user);
    }

    @Override
    public AdminPageResponseVO<AdminPublicPostVO> pagePublicPosts(AdminPublicPostPageQuery query) {
        adminAccessService.assertCurrentUserAdmin();
        Page<PublicPost> page = new Page<>(normalizePageNo(query.getPageNo()), normalizePageSize(query.getPageSize()));
        LambdaQueryWrapper<PublicPost> wrapper = new LambdaQueryWrapper<>();
        String keyword = normalizeNullableText(query.getKeyword());
        if (keyword != null) {
            wrapper.like(PublicPost::getItemName, keyword);
        }
        wrapper.orderByDesc(PublicPost::getCreatedAt);

        Page<PublicPost> result = publicPostMapper.selectPage(page, wrapper);
        Map<Long, UserAccount> userMap = loadUserMap(result);
        return new AdminPageResponseVO<>(
            result.getRecords().stream().map(post -> toPublicPostVO(post, userMap)).toList(),
            result.getCurrent(),
            result.getSize(),
            result.getTotal(),
            result.getPages()
        );
    }

    @Override
    @Transactional
    public void deletePublicPost(Long postId) {
        adminAccessService.assertCurrentUserAdmin();
        PublicPost post = publicPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "Public post not found");
        }
        publicPostFlagMapper.delete(new QueryWrapper<PublicPostFlag>().eq("post_id", postId));
        publicPostMapper.deleteById(postId);
        rollbackLinkedInventoryItem(post);
    }

    private Long sumUntrustedCount() {
        Object value = publicPostMapper.selectObjs(
            new QueryWrapper<PublicPost>().select("COALESCE(SUM(untrusted_count), 0)")
        ).stream().findFirst().orElse(0);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Long sumImageBytes() {
        Object value = fileAssetMapper.selectObjs(
            new QueryWrapper<FileAsset>().select("COALESCE(SUM(file_size), 0)")
        ).stream().findFirst().orElse(0);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private StorageStats resolveStorageStats() {
        try {
            Path uploadRoot = Paths.get(fileProperties.getLocalRoot()).toAbsolutePath().normalize();
            Files.createDirectories(uploadRoot);
            FileStore fileStore = Files.getFileStore(uploadRoot);
            return new StorageStats(fileStore.getTotalSpace(), fileStore.getUsableSpace());
        } catch (IOException | RuntimeException ex) {
            return new StorageStats(0L, 0L);
        }
    }

    private Map<Long, UserAccount> loadUserMap(Page<PublicPost> result) {
        Set<Long> userIds = result.getRecords().stream()
            .map(PublicPost::getUserId)
            .collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userAccountMapper.selectBatchIds(userIds).stream()
            .collect(Collectors.toMap(UserAccount::getId, Function.identity()));
    }

    private void rollbackLinkedInventoryItem(PublicPost post) {
        if (post.getSourceInventoryItemId() == null) {
            return;
        }
        InventoryItem item = inventoryItemMapper.selectById(post.getSourceInventoryItemId());
        if (item == null || item.getPublicPostId() == null || !item.getPublicPostId().equals(post.getId())) {
            return;
        }
        item.setPublicPosted(Boolean.FALSE);
        item.setPublicPostId(null);
        item.setPublicPostedAt(null);
        item.setUpdatedAt(LocalDateTime.now());
        inventoryItemMapper.updateById(item);
    }

    private AdminUserVO toUserVO(UserAccount user) {
        return new AdminUserVO(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getStatus(),
            adminAccessService.isAdmin(user),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }

    private AdminPublicPostVO toPublicPostVO(PublicPost post, Map<Long, UserAccount> userMap) {
        UserAccount user = userMap.get(post.getUserId());
        return new AdminPublicPostVO(
            post.getId(),
            post.getUserId(),
            user == null ? "" : user.getEmail(),
            post.getItemName(),
            post.getPrice(),
            post.getTradeTime(),
            post.getDirection(),
            post.getChannel(),
            post.getCategory(),
            post.getImageFileId(),
            post.getUntrustedCount(),
            post.getCreatedAt()
        );
    }

    private long normalizePageNo(Long pageNo) {
        return pageNo == null ? DEFAULT_PAGE_NO : Math.max(pageNo, DEFAULT_PAGE_NO);
    }

    private long normalizePageSize(Long pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(Math.max(pageSize, 1L), MAX_PAGE_SIZE);
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeRequiredText(String value) {
        String normalized = normalizeNullableText(value);
        if (normalized == null) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Required text field cannot be blank");
        }
        return normalized;
    }

    private void validateUserStatus(String status) {
        if (!USER_STATUS_ACTIVE.equals(status) && !USER_STATUS_DISABLED.equals(status)) {
            throw new BusinessException(BusinessCode.PARAM_INVALID, "Unsupported user status");
        }
    }

    private record StorageStats(Long totalBytes, Long usableBytes) {
    }
}
