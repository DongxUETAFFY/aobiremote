package io.github.dongxuetaffy.aobihelper.publiczone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PublicPostCreateRequest {
    @NotBlank
    @Size(max = 40)
    private String itemName;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @NotNull
    private LocalDate tradeTime;

    @NotBlank
    @Pattern(regexp = "buy|sell")
    private String direction;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
