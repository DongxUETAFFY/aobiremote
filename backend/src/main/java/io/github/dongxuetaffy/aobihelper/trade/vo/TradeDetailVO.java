package io.github.dongxuetaffy.aobihelper.trade.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TradeDetailVO {
    private Long id;
    private String itemName;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal profitAmount;
    private LocalDate buyTime;
    private LocalDate sellTime;
    private LocalDate tradeTime;
    private String channel;
    private String category;
    private String remark;
    private String imageFileId;
    private Boolean publicPosted;
    private Long publicPostId;
    private LocalDateTime publicPostedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigDecimal profitAmount) {
        this.profitAmount = profitAmount;
    }

    public LocalDate getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(LocalDate buyTime) {
        this.buyTime = buyTime;
    }

    public LocalDate getSellTime() {
        return sellTime;
    }

    public void setSellTime(LocalDate sellTime) {
        this.sellTime = sellTime;
    }

    public LocalDate getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(LocalDate tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImageFileId() {
        return imageFileId;
    }

    public void setImageFileId(String imageFileId) {
        this.imageFileId = imageFileId;
    }

    public Boolean getPublicPosted() {
        return publicPosted;
    }

    public void setPublicPosted(Boolean publicPosted) {
        this.publicPosted = publicPosted;
    }

    public Long getPublicPostId() {
        return publicPostId;
    }

    public void setPublicPostId(Long publicPostId) {
        this.publicPostId = publicPostId;
    }

    public LocalDateTime getPublicPostedAt() {
        return publicPostedAt;
    }

    public void setPublicPostedAt(LocalDateTime publicPostedAt) {
        this.publicPostedAt = publicPostedAt;
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
