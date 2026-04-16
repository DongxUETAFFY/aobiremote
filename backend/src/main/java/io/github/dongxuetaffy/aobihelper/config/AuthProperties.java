package io.github.dongxuetaffy.aobihelper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {
    private boolean mailEnabled;
    private boolean devLogVerificationCode;
    private int verificationCodeExpireSeconds = 300;
    private int verificationCodeSendIntervalSeconds = 60;
    private int verificationCodeHourLimit = 10;
    private int loginFailLimit = 5;
    private int loginFailWindowSeconds = 600;

    public boolean isMailEnabled() {
        return mailEnabled;
    }

    public void setMailEnabled(boolean mailEnabled) {
        this.mailEnabled = mailEnabled;
    }

    public boolean isDevLogVerificationCode() {
        return devLogVerificationCode;
    }

    public void setDevLogVerificationCode(boolean devLogVerificationCode) {
        this.devLogVerificationCode = devLogVerificationCode;
    }

    public int getVerificationCodeExpireSeconds() {
        return verificationCodeExpireSeconds;
    }

    public void setVerificationCodeExpireSeconds(int verificationCodeExpireSeconds) {
        this.verificationCodeExpireSeconds = verificationCodeExpireSeconds;
    }

    public int getVerificationCodeSendIntervalSeconds() {
        return verificationCodeSendIntervalSeconds;
    }

    public void setVerificationCodeSendIntervalSeconds(int verificationCodeSendIntervalSeconds) {
        this.verificationCodeSendIntervalSeconds = verificationCodeSendIntervalSeconds;
    }

    public int getVerificationCodeHourLimit() {
        return verificationCodeHourLimit;
    }

    public void setVerificationCodeHourLimit(int verificationCodeHourLimit) {
        this.verificationCodeHourLimit = verificationCodeHourLimit;
    }

    public int getLoginFailLimit() {
        return loginFailLimit;
    }

    public void setLoginFailLimit(int loginFailLimit) {
        this.loginFailLimit = loginFailLimit;
    }

    public int getLoginFailWindowSeconds() {
        return loginFailWindowSeconds;
    }

    public void setLoginFailWindowSeconds(int loginFailWindowSeconds) {
        this.loginFailWindowSeconds = loginFailWindowSeconds;
    }
}
