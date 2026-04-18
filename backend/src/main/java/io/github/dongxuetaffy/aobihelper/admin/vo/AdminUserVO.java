package io.github.dongxuetaffy.aobihelper.admin.vo;

import java.time.LocalDateTime;

public record AdminUserVO(
    Long id,
    String email,
    String nickname,
    String status,
    Boolean admin,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt
) {
}

