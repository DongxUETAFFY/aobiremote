package io.github.dongxuetaffy.aobihelper.inventory.vo;

import java.math.BigDecimal;

public class InventorySummaryVO {
    private BigDecimal totalBuyPrice;
    private Integer obiCount;
    private BigDecimal obiBuyPrice;
    private Integer magicCount;
    private BigDecimal magicBuyPrice;

    public BigDecimal getTotalBuyPrice() {
        return totalBuyPrice;
    }

    public void setTotalBuyPrice(BigDecimal totalBuyPrice) {
        this.totalBuyPrice = totalBuyPrice;
    }

    public Integer getObiCount() {
        return obiCount;
    }

    public void setObiCount(Integer obiCount) {
        this.obiCount = obiCount;
    }

    public BigDecimal getObiBuyPrice() {
        return obiBuyPrice;
    }

    public void setObiBuyPrice(BigDecimal obiBuyPrice) {
        this.obiBuyPrice = obiBuyPrice;
    }

    public Integer getMagicCount() {
        return magicCount;
    }

    public void setMagicCount(Integer magicCount) {
        this.magicCount = magicCount;
    }

    public BigDecimal getMagicBuyPrice() {
        return magicBuyPrice;
    }

    public void setMagicBuyPrice(BigDecimal magicBuyPrice) {
        this.magicBuyPrice = magicBuyPrice;
    }
}
