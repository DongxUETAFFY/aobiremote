package io.github.dongxuetaffy.aobihelper.trade.vo;

import java.math.BigDecimal;

public class TradeSummaryVO {
    private BigDecimal totalBuyAmount;
    private BigDecimal totalSellAmount;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private Integer obiCount;
    private BigDecimal obiBuyAmount;
    private Integer magicCount;
    private BigDecimal magicBuyAmount;

    public BigDecimal getTotalBuyAmount() {
        return totalBuyAmount;
    }

    public void setTotalBuyAmount(BigDecimal totalBuyAmount) {
        this.totalBuyAmount = totalBuyAmount;
    }

    public BigDecimal getTotalSellAmount() {
        return totalSellAmount;
    }

    public void setTotalSellAmount(BigDecimal totalSellAmount) {
        this.totalSellAmount = totalSellAmount;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(BigDecimal totalProfit) {
        this.totalProfit = totalProfit;
    }

    public BigDecimal getTotalLoss() {
        return totalLoss;
    }

    public void setTotalLoss(BigDecimal totalLoss) {
        this.totalLoss = totalLoss;
    }

    public Integer getObiCount() {
        return obiCount;
    }

    public void setObiCount(Integer obiCount) {
        this.obiCount = obiCount;
    }

    public BigDecimal getObiBuyAmount() {
        return obiBuyAmount;
    }

    public void setObiBuyAmount(BigDecimal obiBuyAmount) {
        this.obiBuyAmount = obiBuyAmount;
    }

    public Integer getMagicCount() {
        return magicCount;
    }

    public void setMagicCount(Integer magicCount) {
        this.magicCount = magicCount;
    }

    public BigDecimal getMagicBuyAmount() {
        return magicBuyAmount;
    }

    public void setMagicBuyAmount(BigDecimal magicBuyAmount) {
        this.magicBuyAmount = magicBuyAmount;
    }
}
