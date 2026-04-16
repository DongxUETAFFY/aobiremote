package io.github.dongxuetaffy.aobihelper.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryMarkSoldRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal sellPrice;

    @NotNull
    private LocalDate sellTime;

    @Size(max = 128)
    private String requestId;

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public LocalDate getSellTime() {
        return sellTime;
    }

    public void setSellTime(LocalDate sellTime) {
        this.sellTime = sellTime;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
