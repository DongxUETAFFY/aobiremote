package io.github.dongxuetaffy.aobihelper.auth.vo;

public class RegisterResponseVO {
    private Long userId;

    public RegisterResponseVO() {
    }

    public RegisterResponseVO(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
