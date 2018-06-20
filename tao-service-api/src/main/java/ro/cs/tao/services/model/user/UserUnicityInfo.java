package ro.cs.tao.services.model.user;

/**
 * Entity containing the user unicity details (for unicity checks before adding new user)
 *
 * @author Oana H.
 */
public class UserUnicityInfo {
    private String username;
    private String email;
    private String alternativeEmail;

    public UserUnicityInfo(String username, String email, String alternativeEmail) {
        this.username = username;
        this.email = email;
        this.alternativeEmail = alternativeEmail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlternativeEmail() {
        return alternativeEmail;
    }

    public void setAlternativeEmail(String alternativeEmail) {
        this.alternativeEmail = alternativeEmail;
    }
}
