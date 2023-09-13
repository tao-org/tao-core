package ro.cs.tao.persistence.audit;

import ro.cs.tao.user.User;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

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
            final LocalDateTime now = LocalDateTime.now();
            final boolean isDecrypted = user.getPassword() != null && Crypto.decrypt(user.getPassword(), user.getUsername()).equals(user.getPassword());
            String encryptedPassword = Crypto.encrypt(user.getPassword(), user.getUsername());
            if (user.getModified() == null || Duration.between(user.getModified(), now).getSeconds() > 3 || isDecrypted) {
                user.setPassword(encryptedPassword);
                if (user.getCreated() == null) {
                    user.setCreated(now);
                }
                user.setModified(now);
            }
        }
    }
}
