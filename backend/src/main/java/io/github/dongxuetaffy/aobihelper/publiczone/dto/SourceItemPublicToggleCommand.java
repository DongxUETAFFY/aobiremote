package io.github.dongxuetaffy.aobihelper.publiczone.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SourceItemPublicToggleCommand(
    Long sourceItemId,
    BigDecimal price,
    LocalDate tradeTime,
    String direction,
    String remark,
    String imageFileId
) {
}
