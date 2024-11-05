package ro.cs.tao.security;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.persistence.UserProvider;
import ro.cs.tao.user.Group;
import ro.cs.tao.user.User;
import ro.cs.tao.utils.Crypto;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Base class for modules that handle the authentication of the callers.
 *
 * @author Cosmin Cara
 */
public abstract class TaoLoginModule implements LoginModule {
    protected static UserProvider userProvider;
    protected UserPrincipal userPrincipal;
    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected Map sharedState;
    protected Map options;
    protected boolean succeeded = false;
    protected boolean commitSucceeded = false;
    protected String username;
    protected String password;
    protected String id;
    protected final Logger logger;
    protected final AuthenticationMode configuredAuthMode;

    public static void setUserProvider(UserProvider provider) { userProvider = provider; }

    public TaoLoginModule() {
        logger = Logger.getLogger(getClass().getName());
        this.configuredAuthMode =
                EnumUtils.getEnumConstantByName(AuthenticationMode.class,
                                                ConfigurationManager.getInstance()
                                                                    .getValue("authentication.mode", "local")
                                                                    .toUpperCase());
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        this.succeeded = false;
    }

    @Override
    public boolean login() throws LoginException {
        final boolean localLogin = intendedFor() == AuthenticationMode.LOCAL;
        if (!localLogin && !this.configuredAuthMode.equals(intendedFor())) {
            return false;
        }
        if (callbackHandler == null) {
            throw new LoginException("CallbackHandler null!");
        }

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("name:");
        callbacks[1] = new PasswordCallback("password:", false);

        try {
            // call callback handler
            callbackHandler.handle(callbacks);
        } catch (IOException e) {
            throw new LoginException("IOException calling handle on callbackHandler : " + e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException("UnsupportedCallbackException calling handle on callbackHandler : " + e.getMessage());
        }

        NameCallback nameCallback = (NameCallback) callbacks[0];
        PasswordCallback passwordCallback = (PasswordCallback) callbacks[1];

        username = nameCallback.getName();
        if (shouldEncryptPassword()) {
            password = Crypto.encrypt(new String(passwordCallback.getPassword()), username);
        } else {
            password = new String(passwordCallback.getPassword());
        }
        // verify the username and password
        User user;
        if ((this.configuredAuthMode.equals(intendedFor()) || "admin".equals(username)) && (user = loginImpl(username, password)) != null) {
            succeeded = true;
            id = user.getId();
            return succeeded;
        } else {
            succeeded = false;
            username = null;
            password = null;
            id = null;
            if (localLogin && this.configuredAuthMode != AuthenticationMode.LOCAL) {
                return succeeded;
            } else {
                throw new FailedLoginException("Invalid login credentials.");
            }
        }
    }

    /**
     * This method is called if the LoginContext's overall authentication succeeded
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules succeeded).
     * @return true if this LoginModule's own login and commit attempts succeeded, or false otherwise.
     * @throws LoginException
     */
    @Override
    public boolean commit() throws LoginException {
        if (!succeeded) {
            return false;
        } else {
            // add a Principal (authenticated identity) to the Subject

            // assume the user we authenticated is the SamplePrincipal
            userPrincipal = new UserPrincipal(id,
                                              userProvider.get(id).getGroups().stream().map(Group::getName).collect(Collectors.toSet()));
            subject.getPrincipals().add(userPrincipal);

            // erase username and password values
            username = null;
            password = null;

            commitSucceeded = true;
            return true;
        }
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        succeeded = false;
        succeeded = commitSucceeded;
        username = null;
        password = null;
        userPrincipal = null;
        return true;
    }

    /**
     * This method is called if the LoginContext's overall authentication failed.
     * (the relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not succeed).
     * @return false if this LoginModule's own login and/or commit attempts failed, and true otherwise.
     * @throws LoginException if the abort fails
     */
    @Override
    public boolean abort() throws LoginException {
        if (!succeeded) {
            return false;
        } else if (!commitSucceeded) {
            // login succeeded but overall authentication failed
            succeeded = false;
            username = null;
            password = null;
            userPrincipal = null;
        } else {
            // overall authentication succeeded and commit succeeded, but someone else's commit failed
            logout();
        }
        return true;
    }

    protected abstract AuthenticationMode intendedFor();

    protected abstract boolean shouldEncryptPassword();

    protected abstract User loginImpl(String user, String password);
}
