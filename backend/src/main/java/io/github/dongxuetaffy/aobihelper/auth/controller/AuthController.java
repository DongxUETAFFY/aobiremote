package io.github.dongxuetaffy.aobihelper.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.dongxuetaffy.aobihelper.auth.dto.ChangePasswordRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.LoginRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.RegisterRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.ResetPasswordRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.SendRegisterCodeRequest;
import io.github.dongxuetaffy.aobihelper.auth.service.AuthService;
import io.github.dongxuetaffy.aobihelper.auth.vo.CurrentUserVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.LoginResponseVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.RegisterResponseVO;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-register-code")
    public ApiResponse<Void> sendRegisterCode(
        @Valid @RequestBody SendRegisterCodeRequest request,
        HttpServletRequest httpServletRequest
    ) {
        authService.sendRegisterCode(request.getEmail(), resolveClientIp(httpServletRequest));
        return ApiResponse.success("Verification code sent", null);
    }

    @PostMapping("/send-reset-password-code")
    public ApiResponse<Void> sendResetPasswordCode(
        @Valid @RequestBody SendRegisterCodeRequest request,
        HttpServletRequest httpServletRequest
    ) {
        authService.sendResetPasswordCode(request.getEmail(), resolveClientIp(httpServletRequest));
        return ApiResponse.success("Verification code sent", null);
    }

    @SaCheckLogin
    @GetMapping("/me")
    public ApiResponse<CurrentUserVO> currentUser() {
        return ApiResponse.success(authService.getCurrentUser());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseVO> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.success("Login success", authService.login(request, resolveClientIp(httpServletRequest)));
    }

    @PostMapping("/register")
    public ApiResponse<RegisterResponseVO> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success("Register success", authService.register(request));
    }

    @SaCheckLogin
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success("Logout success", null);
    }

    @SaCheckLogin
    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success("Password changed successfully", null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Password reset successfully", null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        String forwardedIp = firstForwardedIp(forwardedFor);
        if (forwardedIp != null) {
            return forwardedIp;
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private String firstForwardedIp(String forwardedFor) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }
        String first = forwardedFor.split(",", 2)[0].trim();
        return first.isBlank() ? null : first;
    }
}
