package ro.cs.tao.persistence.audit;

import ro.cs.tao.user.User;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;

public class UserListener {
    @PostLoad
    @PostPersist
    @PostUpdate
    public void onLoad(User user) {
        if (user != null) {
            String decryptedPassword = Crypto.decrypt(user.getPassword(), user.getUsername());
            user.setPassword(decryptedPassword);
        }
    }

    @PrePersist
    @PreUpdate
    public void onBeforeSave(User user) {
        if (user != null) {
            String encryptedPassword = Crypto.encrypt(user.getPassword(), user.getUsername());
            user.setPassword(encryptedPassword);
        }
    }
}
