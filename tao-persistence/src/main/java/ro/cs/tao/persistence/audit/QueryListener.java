package ro.cs.tao.persistence.audit;

import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;

public class QueryListener {

    @PostLoad
    @PostPersist
    @PostUpdate
    public void onLoad(Query query) {
        if (query != null) {
            String decryptedPassword = Crypto.decrypt(query.getPassword(), query.getUser());
            query.setPassword(decryptedPassword);
        }
    }

    @PrePersist
    @PreUpdate
    public void onBeforeSave(Query query) {
        if (query != null) {
            String encryptedPassword = Crypto.encrypt(query.getPassword(), query.getUser());
            query.setPassword(encryptedPassword);
        }
    }
}
