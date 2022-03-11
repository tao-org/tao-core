package ro.cs.tao.component;

import ro.cs.tao.component.enums.AuthenticationType;

public class WPSAuthentication extends StringIdentifiable {
    private AuthenticationType type;
    private String user;
    private String password;
    private String loginUrl;
    private String authHeader;

    public WPSAuthentication() {
        super();
    }

    public AuthenticationType getType() {
        return type;
    }

    public void setType(AuthenticationType type) {
        this.type = type;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }
}
