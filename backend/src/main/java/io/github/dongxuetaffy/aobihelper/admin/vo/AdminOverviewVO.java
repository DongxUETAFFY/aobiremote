package io.github.dongxuetaffy.aobihelper.admin.vo;

public record AdminOverviewVO(
    Long totalUsers,
    Long activeUsers,
    Long disabledUsers,
    Long todayNewUsers,
    Long totalInventoryItems,
    Long totalPublicPosts,
    Long todayPublicPosts,
    Long totalFiles,
    Long totalImageCount,
    Long totalImageBytes,
    Long totalStorageBytes,
    Long usableStorageBytes,
    Long totalUntrustedCount,
    Double qps
) {
}
