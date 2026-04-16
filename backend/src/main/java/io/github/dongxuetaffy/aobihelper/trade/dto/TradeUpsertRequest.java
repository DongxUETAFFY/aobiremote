package io.github.dongxuetaffy.aobihelper.trade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TradeUpsertRequest {
    @NotBlank
    @Size(max = 40)
    private String itemName;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal buyPrice;

    @NotNull
    private LocalDate buyTime;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal sellPrice;

    @NotNull
    private LocalDate sellTime;

    @NotBlank
    @Pattern(regexp = "xianyu|tieba|other")
    private String channel;

    @NotBlank
    @Pattern(regexp = "magic|obi")
    private String category;

    @Size(max = 15)
    private String remark;

    @Size(max = 255)
    private String imageFileId;

    @Size(max = 128)
    private String requestId;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
