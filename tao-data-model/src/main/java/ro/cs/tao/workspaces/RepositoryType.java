package ro.cs.tao.workspaces;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlEnum(Integer.class)
public enum RepositoryType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local repository", "file", "root",
          new LinkedHashMap<String, Boolean>(){{
              put("root", true);
    }}, true),
    @XmlEnumValue("2")
    AWS(2, "AWS S3 repository", "s3", "aws.bucket",
        new LinkedHashMap<String, Boolean>() {{
            put("aws.region", true); put("aws.access.key", false);
            put("aws.secret.key", false); put("aws.bucket", true);
    }}, false),
    @XmlEnumValue("3")
    SWIFT(3, "OpenStack Swift S3 repository", "swift", "openstack.bucket",
          new LinkedHashMap<String, Boolean>() {{
              put("openstack.auth.url", true); put("openstack.tenantId", true); put("openstack.domain", true);
              put("openstack.user", true); put("openstack.password", true); put("openstack.bucket", true);
    }}, false),
    @XmlEnumValue("3")
    STAC(4, "STAC repository", "stac", "stac.url",
         new LinkedHashMap<String, Boolean>() {{
             put("stac.url", true);
    }}, false);
    private final int value;
    private final String description;
    private final String prefix;
    private final LinkedHashMap<String, Boolean> parameters;
    private final String rootKey;
    private final boolean singleton;

    RepositoryType(int value, String description, String prefix, String rootKey, LinkedHashMap<String, Boolean> parameters, boolean singleton) {
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

    public Map<String, Boolean> getParameters() { return new LinkedHashMap<>(parameters); }

    public boolean singleton() {return this.singleton; }
    
    public static RepositoryType fromPrefix(String prefix) {
        for (RepositoryType type : RepositoryType.values()) {
            if (type.prefix.equals(prefix)) {
                return type;
            }
        }
        return null;
    }
}
