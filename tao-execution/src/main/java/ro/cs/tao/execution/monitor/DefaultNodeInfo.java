package ro.cs.tao.execution.monitor;

public class DefaultNodeInfo {
    private final String namePrefix;
    private final String description;
    private final String user;
    private final String password;
    private final String sshKey;

    public DefaultNodeInfo(String namePrefix, String description, String user, String password, String key) {
        this.namePrefix = namePrefix;
        this.description = description;
        this.user = user;
        this.password = password;
        this.sshKey = key;
    }

    public String getNamePrefix() { return namePrefix; }

    public String getDescription() { return description; }

    public String getUser() { return user; }

    public String getPassword() { return password; }

    public String getSshKey() {
        return sshKey;
    }
}
