package io.github.dongxuetaffy.aobihelper.auth.service;

import io.github.dongxuetaffy.aobihelper.auth.dto.ChangePasswordRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.LoginRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.RegisterRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.ResetPasswordRequest;
import io.github.dongxuetaffy.aobihelper.auth.vo.CurrentUserVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.LoginResponseVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.RegisterResponseVO;

public interface AuthService {
    void sendRegisterCode(String email, String requestIp);

    void sendResetPasswordCode(String email, String requestIp);

    RegisterResponseVO register(RegisterRequest request);

    LoginResponseVO login(LoginRequest request, String requestIp);

    void logout();

    CurrentUserVO getCurrentUser();

    void changePassword(ChangePasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
