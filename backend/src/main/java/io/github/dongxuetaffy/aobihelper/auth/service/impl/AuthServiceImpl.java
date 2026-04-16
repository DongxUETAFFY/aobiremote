package io.github.dongxuetaffy.aobihelper.auth.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.dongxuetaffy.aobihelper.auth.cache.AuthCacheService;
import io.github.dongxuetaffy.aobihelper.auth.dto.ChangePasswordRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.LoginRequest;
import io.github.dongxuetaffy.aobihelper.auth.dto.RegisterRequest;
import io.github.dongxuetaffy.aobihelper.auth.entity.UserAccount;
import io.github.dongxuetaffy.aobihelper.auth.mapper.UserAccountMapper;
import io.github.dongxuetaffy.aobihelper.auth.service.AuthService;
import io.github.dongxuetaffy.aobihelper.auth.vo.CurrentUserVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.LoginResponseVO;
import io.github.dongxuetaffy.aobihelper.auth.vo.RegisterResponseVO;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import io.github.dongxuetaffy.aobihelper.config.AuthProperties;
import io.github.dongxuetaffy.aobihelper.stats.entity.UserStats;
import io.github.dongxuetaffy.aobihelper.stats.mapper.UserStatsMapper;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String USER_STATUS_ACTIVE = "active";
    private static final String USER_STATUS_DISABLED = "disabled";

    private final UserAccountMapper userAccountMapper;
    private final UserStatsMapper userStatsMapper;
    private final AuthCacheService authCacheService;
    private final JavaMailSender javaMailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;

    @Override
    public void sendRegisterCode(String email, String requestIp) {
        String normalizedEmail = normalizeEmail(email);
        ensureEmailNotRegistered(normalizedEmail);
        validateSendCodeRateLimit(normalizedEmail);

        String code = generateVerificationCode();
        authCacheService.storeRegisterCode(
            normalizedEmail,
            code,
            Duration.ofSeconds(authProperties.getVerificationCodeExpireSeconds())
        );
        authCacheService.markRegisterInterval(
            normalizedEmail,
            Duration.ofSeconds(authProperties.getVerificationCodeSendIntervalSeconds())
        );
        authCacheService.incrementRegisterHourlyCount(normalizedEmail, Duration.ofHours(1));
        dispatchRegisterCode(normalizedEmail, code, requestIp);
    }

    @Override
    @Transactional
    public RegisterResponseVO register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        ensureEmailNotRegistered(normalizedEmail);

        String cachedCode = authCacheService.getRegisterCode(normalizedEmail);
        if (!Objects.equals(cachedCode, request.getCode())) {
            throw new BusinessException(BusinessCode.VERIFICATION_CODE_INVALID, "Verification code is invalid or expired");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(normalizedEmail);
        userAccount.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userAccount.setNickname(buildDefaultNickname(normalizedEmail));
        userAccount.setAvatarUrl("");
        userAccount.setStatus(USER_STATUS_ACTIVE);
        userAccount.setCreatedAt(LocalDateTime.now());
        userAccount.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.insert(userAccount);

        UserStats userStats = new UserStats();
        userStats.setUserId(userAccount.getId());
        userStats.setTotalProfit(BigDecimal.ZERO);
        userStats.setTotalLoss(BigDecimal.ZERO);
        userStats.setSoldCount(0);
        userStats.setSoldBuyTotal(BigDecimal.ZERO);
        userStats.setSoldSellTotal(BigDecimal.ZERO);
        userStats.setUnsoldCount(0);
        userStats.setUnsoldBuyTotal(BigDecimal.ZERO);
        userStats.setCreatedAt(LocalDateTime.now());
        userStats.setUpdatedAt(LocalDateTime.now());
        userStatsMapper.insert(userStats);

        authCacheService.removeRegisterCode(normalizedEmail);
        return new RegisterResponseVO(userAccount.getId());
    }

    @Override
    public LoginResponseVO login(LoginRequest request, String requestIp) {
        String normalizedEmail = normalizeEmail(request.getEmail());
        validateLoginRateLimit(normalizedEmail, requestIp);

        UserAccount user = getUserByEmail(normalizedEmail);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            recordLoginFailure(normalizedEmail, requestIp);
            throw new BusinessException(BusinessCode.LOGIN_FAILED, "Email or password is incorrect");
        }
        if (USER_STATUS_DISABLED.equals(user.getStatus())) {
            throw new BusinessException(BusinessCode.ACCOUNT_DISABLED, "Account is disabled");
        }

        clearLoginFailures(normalizedEmail, requestIp);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(user);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginResponseVO responseVO = new LoginResponseVO();
        responseVO.setToken(tokenInfo.getTokenValue());
        responseVO.setTokenName(tokenInfo.getTokenName());
        responseVO.setUser(toCurrentUser(user));
        return responseVO;
    }

    @Override
    public void logout() {
        StpUtil.checkLogin();
        StpUtil.logout();
    }

    @Override
    public CurrentUserVO getCurrentUser() {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectById(userId);
        if (userAccount == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }
        return toCurrentUser(userAccount);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        StpUtil.checkLogin();
        long userId = StpUtil.getLoginIdAsLong();
        UserAccount userAccount = userAccountMapper.selectById(userId);
        if (userAccount == null) {
            throw new BusinessException(BusinessCode.RECORD_NOT_FOUND, "User not found");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), userAccount.getPasswordHash())) {
            throw new BusinessException(BusinessCode.LOGIN_FAILED, "Old password is incorrect");
        }
        userAccount.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userAccount.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.updateById(userAccount);
        StpUtil.logout(userId);
    }

    private void validateSendCodeRateLimit(String normalizedEmail) {
        if (authCacheService.hasRegisterInterval(normalizedEmail)) {
            throw new BusinessException(BusinessCode.TOO_FREQUENT, "Verification code was sent too frequently");
        }
        int hourlyCount = authCacheService.getRegisterHourlyCount(normalizedEmail);
        if (hourlyCount >= authProperties.getVerificationCodeHourLimit()) {
            throw new BusinessException(BusinessCode.TOO_FREQUENT, "Verification code send limit reached");
        }
    }

    private void validateLoginRateLimit(String normalizedEmail, String requestIp) {
        int failLimit = authProperties.getLoginFailLimit();
        int emailFailCount = authCacheService.getLoginFailEmailCount(normalizedEmail);
        int ipFailCount = requestIp == null ? 0 : authCacheService.getLoginFailIpCount(requestIp);
        if (emailFailCount >= failLimit || ipFailCount >= failLimit) {
            throw new BusinessException(BusinessCode.TOO_FREQUENT, "Too many login failures, please try again later");
        }
    }

    private void recordLoginFailure(String normalizedEmail, String requestIp) {
        authCacheService.incrementLoginFailEmailCount(
            normalizedEmail,
            Duration.ofSeconds(authProperties.getLoginFailWindowSeconds())
        );
        if (requestIp != null && !requestIp.isBlank()) {
            authCacheService.incrementLoginFailIpCount(
                requestIp,
                Duration.ofSeconds(authProperties.getLoginFailWindowSeconds())
            );
        }
    }

    private void clearLoginFailures(String normalizedEmail, String requestIp) {
        authCacheService.clearLoginFailEmailCount(normalizedEmail);
        if (requestIp != null && !requestIp.isBlank()) {
            authCacheService.clearLoginFailIpCount(requestIp);
        }
    }

    private void dispatchRegisterCode(String email, String code, String requestIp) {
        if (!authProperties.isMailEnabled()) {
            if (authProperties.isDevLogVerificationCode()) {
                log.info("Register code for {} is {}. requestIp={}", email, code, requestIp);
            }
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject("Aobi Helper Register Code");
            helper.setText(buildRegisterCodeMailContent(code), false);
            helper.setFrom(new InternetAddress("no-reply@example.com"));
            javaMailSender.send(message);
        } catch (Exception exception) {
            log.error("Failed to send register code email to {}", email, exception);
            throw new BusinessException(BusinessCode.STATE_INVALID, "Failed to send verification code");
        }
    }

    private String buildRegisterCodeMailContent(String code) {
        return "Welcome to Aobi Helper Web.\n\n"
            + "Your verification code is: " + code + "\n"
            + "The code is valid for 5 minutes.";
    }

    private void ensureEmailNotRegistered(String normalizedEmail) {
        if (getUserByEmail(normalizedEmail) != null) {
            throw new BusinessException(BusinessCode.EMAIL_ALREADY_REGISTERED, "Email has already been registered");
        }
    }

    private UserAccount getUserByEmail(String normalizedEmail) {
        return userAccountMapper.selectOne(
            new LambdaQueryWrapper<UserAccount>()
                .eq(UserAccount::getEmail, normalizedEmail)
                .last("limit 1")
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildDefaultNickname(String email) {
        String prefix = email.substring(0, email.indexOf('@'));
        String trimmed = prefix.length() > 20 ? prefix.substring(0, 20) : prefix;
        return trimmed.isBlank() ? "Aobi User" : trimmed;
    }

    private String generateVerificationCode() {
        int value = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return Integer.toString(value);
    }

    private CurrentUserVO toCurrentUser(UserAccount userAccount) {
        CurrentUserVO currentUserVO = new CurrentUserVO();
        currentUserVO.setId(userAccount.getId());
        currentUserVO.setEmail(userAccount.getEmail());
        currentUserVO.setNickname(userAccount.getNickname());
        currentUserVO.setAvatarUrl(userAccount.getAvatarUrl());
        return currentUserVO;
    }

}
