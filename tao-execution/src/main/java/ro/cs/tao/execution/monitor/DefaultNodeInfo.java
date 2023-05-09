package ro.cs.tao.execution.monitor;

public class DefaultNodeInfo {
    private final String namePrefix;
    private final String description;
    private final String user;
    private final String password;

    public DefaultNodeInfo(String namePrefix, String description, String user, String password) {
        this.namePrefix = namePrefix;
        this.description = description;
        this.user = user;
        this.password = password;
    }

    public String getNamePrefix() { return namePrefix; }

    public String getDescription() { return description; }

    public String getUser() { return user; }

    public String getPassword() { return password; }
}
