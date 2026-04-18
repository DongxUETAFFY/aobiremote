package io.github.dongxuetaffy.aobihelper.admin.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminPublicPostVO(
    Long id,
    Long userId,
    String userEmail,
    String itemName,
    BigDecimal price,
    LocalDate tradeTime,
    String direction,
    String channel,
    String category,
    String imageFileId,
    Integer untrustedCount,
    LocalDateTime createdAt
) {
}

