package io.github.dongxuetaffy.aobihelper.publiczone.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PublicPostListItemVO {
    private Long id;
    private String itemName;
    private BigDecimal price;
    private LocalDate tradeTime;
    private String direction;
    private String channel;
    private String category;
    private String remark;
    private String imageFileId;
    private Integer untrustedCount;
    private Boolean untrusted;
    private String publisherName;
    private String publisherAvatar;
    private Boolean mine;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(LocalDate tradeTime) {
        this.tradeTime = tradeTime;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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

    public Integer getUntrustedCount() {
        return untrustedCount;
    }

    public void setUntrustedCount(Integer untrustedCount) {
        this.untrustedCount = untrustedCount;
    }

    public Boolean getUntrusted() {
        return untrusted;
    }

    public void setUntrusted(Boolean untrusted) {
        this.untrusted = untrusted;
    }

    public String getPublisherName() {
        return publisherName;
    }

    public void setPublisherName(String publisherName) {
        this.publisherName = publisherName;
    }

    public String getPublisherAvatar() {
        return publisherAvatar;
    }

    public void setPublisherAvatar(String publisherAvatar) {
        this.publisherAvatar = publisherAvatar;
    }

    public Boolean getMine() {
        return mine;
    }

    public void setMine(Boolean mine) {
        this.mine = mine;
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
