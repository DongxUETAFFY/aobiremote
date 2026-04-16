package io.github.dongxuetaffy.aobihelper.auth.vo;

public class LoginResponseVO {
    private String token;
    private String tokenName;
    private CurrentUserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public CurrentUserVO getUser() {
        return user;
    }

    public void setUser(CurrentUserVO user) {
        this.user = user;
    }
}
