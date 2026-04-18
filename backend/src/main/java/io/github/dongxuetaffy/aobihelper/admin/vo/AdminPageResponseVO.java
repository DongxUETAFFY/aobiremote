package io.github.dongxuetaffy.aobihelper.admin.vo;

import java.util.List;

public record AdminPageResponseVO<T>(
    List<T> items,
    Long pageNo,
    Long pageSize,
    Long totalCount,
    Long totalPages
) {
}

