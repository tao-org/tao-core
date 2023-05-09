package ro.cs.tao.persistence.managers;

import org.springframework.stereotype.Component;
import ro.cs.tao.component.WebServiceAuthentication;
import ro.cs.tao.component.enums.AuthenticationType;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.WPSAuthenticationProvider;
import ro.cs.tao.persistence.repository.WPSAuthenticationRepository;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.StringUtilities;

@Component("wpsAuthenticationManager")
public class WPSAuthenticationManager
        extends EntityManager<WebServiceAuthentication, String, WPSAuthenticationRepository>
        implements WPSAuthenticationProvider {

    @Override
    public WebServiceAuthentication get(String id) {
        final WebServiceAuthentication authentication = super.get(id);
        if (authentication != null && authentication.getUser() != null) {
            authentication.setPassword(Crypto.decrypt(authentication.getPassword(), authentication.getUser()));
        }
        return authentication;
    }

    @Override
    public WebServiceAuthentication save(WebServiceAuthentication entity) throws PersistenceException {
        if (entity.getType() != AuthenticationType.NONE) {
            if (entity.getUser() != null) {
                entity.setPassword(Crypto.encrypt(entity.getPassword(), entity.getUser()));
            }
            return super.save(entity);
        } else {
            // Don't persist the NONE authentication since it's irrelevant
            return entity;
        }
    }

    @Override
    public WebServiceAuthentication update(WebServiceAuthentication entity) throws PersistenceException {
        if (entity.getType() != AuthenticationType.NONE) {
            if (entity.getUser() != null) {
                entity.setPassword(Crypto.encrypt(entity.getPassword(), entity.getUser()));
            }
            return super.update(entity);
        } else {
            return entity;
        }
    }

    @Override
    protected String identifier() {
        return "id";
    }

    @Override
    protected boolean checkEntity(WebServiceAuthentication entity) {
        return !StringUtilities.isNullOrEmpty(entity.getId())
                && entity.getType() != null
                && (entity.getType() == AuthenticationType.NONE ||
                    (!StringUtilities.isNullOrEmpty(entity.getUser())
                            && !StringUtilities.isNullOrEmpty(entity.getPassword())
                            && !StringUtilities.isNullOrEmpty(entity.getLoginUrl())
                            && (entity.getType() == AuthenticationType.BASIC
                                || !StringUtilities.isNullOrEmpty(entity.getAuthHeader()))));
    }
}
