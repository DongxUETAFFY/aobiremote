package io.github.dongxuetaffy.aobihelper.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("inventory_item")
public class InventoryItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String itemName;
    private BigDecimal buyPrice;
    private LocalDate buyTime;
    private BigDecimal sellPrice;
    private LocalDate sellTime;
    private LocalDate tradeTime;
    private String channel;
    private String category;
    private String status;
    private BigDecimal profitAmount;
    private Boolean publicPosted;
    private Long publicPostId;
    private LocalDateTime publicPostedAt;
    private String remark;
    private String imageFileId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDate getBuyTime() {
        return buyTime;
    }

    public void setBuyTime(LocalDate buyTime) {
        this.buyTime = buyTime;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getProfitAmount() {
        return profitAmount;
    }

    public void setProfitAmount(BigDecimal profitAmount) {
        this.profitAmount = profitAmount;
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
