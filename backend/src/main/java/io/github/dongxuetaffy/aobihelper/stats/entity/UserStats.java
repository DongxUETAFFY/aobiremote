package io.github.dongxuetaffy.aobihelper.stats.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("user_stats")
public class UserStats {
    @TableId
    private Long userId;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private Integer soldCount;
    private BigDecimal soldBuyTotal;
    private BigDecimal soldSellTotal;
    private Integer unsoldCount;
    private BigDecimal unsoldBuyTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public BigDecimal getSoldBuyTotal() {
        return soldBuyTotal;
    }

    public void setSoldBuyTotal(BigDecimal soldBuyTotal) {
        this.soldBuyTotal = soldBuyTotal;
    }

    public BigDecimal getSoldSellTotal() {
        return soldSellTotal;
    }

    public void setSoldSellTotal(BigDecimal soldSellTotal) {
        this.soldSellTotal = soldSellTotal;
    }

    public Integer getUnsoldCount() {
        return unsoldCount;
    }

    public void setUnsoldCount(Integer unsoldCount) {
        this.unsoldCount = unsoldCount;
    }

    public BigDecimal getUnsoldBuyTotal() {
        return unsoldBuyTotal;
    }

    public void setUnsoldBuyTotal(BigDecimal unsoldBuyTotal) {
        this.unsoldBuyTotal = unsoldBuyTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
