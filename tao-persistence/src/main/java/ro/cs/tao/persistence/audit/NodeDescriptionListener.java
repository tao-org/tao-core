package ro.cs.tao.persistence.audit;

import ro.cs.tao.persistence.NodeDBProvider;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.Crypto;

import javax.persistence.*;

public class NodeDescriptionListener {

    @PostLoad
    @PostPersist
    public void onLoad(NodeDescription node) {
        decryptUserPass(node);
    }

    @PostUpdate
    public void afterUpdate(NodeDescription node) {
        decryptUserPass(node);
        SpringContextBridge.services().getService(NodeDBProvider.class).updateModifiedDate(node.getId());
    }

    @PrePersist
    @PreUpdate
    public void onBeforeSave(NodeDescription node) {
        if (node != null) {
            String encryptedPassword = Crypto.encrypt(node.getUserPass(), node.getUserName());
            node.setUserPass(encryptedPassword);
        }
    }

    private static void decryptUserPass(NodeDescription node) {
        if (node != null) {
            String decryptedPassword = Crypto.decrypt(node.getUserPass(), node.getUserName());
            node.setUserPass(decryptedPassword);
        }
    }
}
