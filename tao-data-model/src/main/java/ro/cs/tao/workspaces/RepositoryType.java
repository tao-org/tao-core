package ro.cs.tao.workspaces;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlEnum(Integer.class)
public enum RepositoryType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local repository", "file", "root", null, null,
          new LinkedHashMap<String, Boolean>(){{
              put("root", true);
    }}, true),
    @XmlEnumValue("2")
    AWS(2, "AWS S3 repository", "s3", "aws.bucket", null, null,
        new LinkedHashMap<String, Boolean>() {{
            put("aws.region", true); put("aws.access.key", false);
            put("aws.secret.key", false); put("aws.bucket", true);
    }}, false),
    @XmlEnumValue("3")
    SWIFT(3, "OpenStack Swift S3 repository", "swift", "openstack.bucket", "openstack.user", "openstack.password",
          new LinkedHashMap<String, Boolean>() {{
              put("openstack.auth.url", true); put("openstack.tenantId", true); put("openstack.domain", true);
              put("openstack.user", true); put("openstack.password", true); put("openstack.bucket", true);
    }}, false),
    @XmlEnumValue("4")
    STAC(4, "STAC repository", "stac", "stac.url", null, null,
         new LinkedHashMap<String, Boolean>() {{
             put("stac.url", true); put("page.size", false);
    }}, false),
    @XmlEnumValue("5")
    SMB(5, "Network share repository", "smb", "smb.share", "smb.user", "smb.password",
                 new LinkedHashMap<String, Boolean>() {{
        put("smb.server", true);
        put("smb.share", true);
        put("smb.domain", false);
        put("smb.user", false);
        put("smb.password", false);
    }}, false),
    @XmlEnumValue("6")
    FTP(6, "FTP repository", "ftp", "ftp.server", "ftp.user", "ftp.password",
        new LinkedHashMap<String, Boolean>() {{
            put("ftp.server", true);
            put("ftp.user", false);
            put("ftp.password", false);
        }}, false),
    @XmlEnumValue("7")
    FTPS(7, "FTPS repository", "ftps", "ftps.server", "ftps.user", "ftps.password",
        new LinkedHashMap<String, Boolean>() {{
            put("ftps.server", true);
            put("ftps.user", false);
            put("ftps.password", false);
        }}, false);
    private final int value;
    private final String description;
    private final String prefix;
    private final LinkedHashMap<String, Boolean> parameters;
    private final String rootKey;
    private final String userKey;
    private final String pwdKey;
    private final boolean singleton;

    RepositoryType(int value, String description, String prefix, String rootKey, String userKey, String pwdKey,
                   LinkedHashMap<String, Boolean> parameters, boolean singleton) {
        this.value = value;
        this.description = description;
        this.prefix = prefix;
        this.rootKey = rootKey;
        this.userKey = userKey;
        this.pwdKey = pwdKey;
        this.parameters = parameters;
        this.singleton = singleton;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    public String prefix() { return this.prefix; }

    public String rootKey() { return this.rootKey; }

    public String userKey() { return this.userKey; }

    public String passwordKey() { return this.pwdKey; }

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
