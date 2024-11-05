package ro.cs.tao.subscription;

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.user.User;

import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ExternalResourceSubscription extends LongIdentifiable {
    private String name;
    private Map<String, ExternalFlavorSubscription> flavors;
    private Integer objectStorageGB;
    private Set<User> users;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ExternalFlavorSubscription> getFlavors() {
        if (this.flavors == null) {
            this.flavors = new LinkedHashMap<>();
        }
        return this.flavors;
    }

    public void setFlavors(Map<String, ExternalFlavorSubscription> flavors) {
        this.flavors = flavors;
    }

    public void addFlavor(ExternalFlavorSubscription flavorSubscription) {
        if (this.flavors == null) {
            this.flavors = new LinkedHashMap<>();
        }
        this.flavors.put(flavorSubscription.getFlavorId(), flavorSubscription);
    }

    public void removeFlavor(String flavorId) {
        if (this.flavors != null) {
            this.flavors.remove(flavorId);
        }
    }

    public Integer getObjectStorageGB() {
        return objectStorageGB;
    }

    public void setObjectStorageGB(Integer objectStorageGB) {
        this.objectStorageGB = objectStorageGB;
    }

    /**
     * Returns the users of this subscription.
     */
    @XmlElementWrapper(name = "users")
    public Set<User> getUsers() {
        if (this.users == null) {
            this.users = new LinkedHashSet<>();
        }
        return this.users;
    }

    /**
     * Sets the parameter users of this subscription.
     */
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        final Set<User> users = getUsers();
        users.add(user);
    }

}
