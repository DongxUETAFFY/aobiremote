package io.github.dongxuetaffy.aobihelper.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AdminUpdateUserStatusRequest {
    @NotBlank
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

