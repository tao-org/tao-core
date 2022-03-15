package ro.cs.tao.topology;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.HashSet;
import java.util.Set;

@XmlEnum(Integer.class)
public enum WorkspaceType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local repository", "file", "root", new HashSet<String>(){{
        add("root");
    }}),
    @XmlEnumValue("2")
    AWS(2, "AWS S3 repository", "s3", "aws.bucket", new HashSet<String>() {{
        add("aws.region"); add("aws.access.key"); add("aws.secret.key"); add("aws.bucket");
    }}),
    @XmlEnumValue("3")
    SWIFT(3, "OpenStack Swift S3 repository", "swift", "openstack.bucket", new HashSet<String>() {{
        add("openstack.auth.url"); add("openstack.tenantId"); add("openstack.domain");
        add("openstack.user"); add("openstack.password"); add("openstack.bucket");
    }});
    private final int value;
    private final String description;
    private final String prefix;
    private final Set<String> parameters;
    private final String rootKey;

    WorkspaceType(int value, String description, String prefix, String rootKey, Set<String> parameters) {
        this.value = value;
        this.description = description;
        this.prefix = prefix;
        this.rootKey = rootKey;
        this.parameters = parameters;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    public String prefix() { return this.prefix; }

    public String rootKey() { return this.rootKey; }

    public Set<String> getParameters() { return new HashSet<>(parameters); }
    
    public static WorkspaceType fromPrefix(String prefix) {
        for (WorkspaceType type : WorkspaceType.values()) {
            if (type.prefix.equals(prefix)) {
                return type;
            }
        }
        return null;
    }
}
