package io.github.dongxuetaffy.aobihelper.common.guard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("operation_guard")
public class OperationGuard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String action;
    private String guardKey;
    private String lastRequestId;
    private LocalDateTime lastSubmitAt;
    private String recentTimesJson;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getGuardKey() {
        return guardKey;
    }

    public void setGuardKey(String guardKey) {
        this.guardKey = guardKey;
    }

    public String getLastRequestId() {
        return lastRequestId;
    }

    public void setLastRequestId(String lastRequestId) {
        this.lastRequestId = lastRequestId;
    }

    public LocalDateTime getLastSubmitAt() {
        return lastSubmitAt;
    }

    public void setLastSubmitAt(LocalDateTime lastSubmitAt) {
        this.lastSubmitAt = lastSubmitAt;
    }

    public String getRecentTimesJson() {
        return recentTimesJson;
    }

    public void setRecentTimesJson(String recentTimesJson) {
        this.recentTimesJson = recentTimesJson;
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
