package ro.cs.tao.persistence.audit;

import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;

public class NodeDescriptionListener {
    @PostLoad
    @PostPersist
    @PostUpdate
    public void onLoad(NodeDescription node) {
        if (node != null) {
            String decryptedPassword = Crypto.decrypt(node.getUserPass(), node.getUserName());
            node.setUserPass(decryptedPassword);
        }
    }

    @PrePersist
    @PreUpdate
    public void onBeforeSave(NodeDescription node) {
        if (node != null) {
            String encryptedPassword = Crypto.encrypt(node.getUserPass(), node.getUserName());
            node.setUserPass(encryptedPassword);
        }
    }
}
