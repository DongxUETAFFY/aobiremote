package io.github.dongxuetaffy.aobihelper.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank
    @Size(min = 8, max = 64)
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 64)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
