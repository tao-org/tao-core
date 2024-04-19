package ro.cs.tao.persistence.audit;

import ro.cs.tao.datasource.DataSourceCredentials;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;

public class DataSourceCredentialsListener {

    @PostLoad
    @PostPersist
    @PostUpdate
    public void onLoad(DataSourceCredentials credentials) {
        if (credentials != null) {
            String decryptedPassword = Crypto.decrypt(credentials.getPassword(), credentials.getUserName());
            credentials.setPassword(decryptedPassword);
            String decryptedSecret = Crypto.decrypt(credentials.getSecret(), credentials.getUserName());
            credentials.setSecret(decryptedSecret);
        }
    }

    @PrePersist
    @PreUpdate
    public void onBeforeSave(DataSourceCredentials credentials) {
        if (credentials != null) {
            String encryptedPassword = Crypto.encrypt(credentials.getPassword(), credentials.getUserName());
            credentials.setPassword(encryptedPassword);
            String encryptedSecret = Crypto.encrypt(credentials.getSecret(), credentials.getUserName());
            credentials.setSecret(encryptedSecret);
        }
    }
}
