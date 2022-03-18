package ro.cs.tao.topology;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.ArrayList;
import java.util.List;

@XmlEnum(Integer.class)
public enum WorkspaceType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local repository", "file", "root", new ArrayList<String>(){{
        add("root");
    }}, true),
    @XmlEnumValue("2")
    AWS(2, "AWS S3 repository", "s3", "aws.bucket", new ArrayList<String>() {{
        add("aws.region"); add("aws.access.key"); add("aws.secret.key"); add("aws.bucket");
    }}, false),
    @XmlEnumValue("3")
    SWIFT(3, "OpenStack Swift S3 repository", "swift", "openstack.bucket",  new ArrayList<String>() {{
        add("openstack.auth.url"); add("openstack.tenantId"); add("openstack.domain");
        add("openstack.user"); add("openstack.password"); add("openstack.bucket");
    }}, false);
    private final int value;
    private final String description;
    private final String prefix;
    private final List<String> parameters;
    private final String rootKey;
    private final boolean singleton;

    WorkspaceType(int value, String description, String prefix, String rootKey, List<String> parameters, boolean singleton) {
        this.value = value;
        this.description = description;
        this.prefix = prefix;
        this.rootKey = rootKey;
        this.parameters = parameters;
        this.singleton = singleton;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    public String prefix() { return this.prefix; }

    public String rootKey() { return this.rootKey; }

    public List<String> getParameters() { return new ArrayList<>(parameters); }

    public boolean singleton() {return this.singleton; }
    
    public static WorkspaceType fromPrefix(String prefix) {
        for (WorkspaceType type : WorkspaceType.values()) {
            if (type.prefix.equals(prefix)) {
                return type;
            }
        }
        return null;
    }
}
