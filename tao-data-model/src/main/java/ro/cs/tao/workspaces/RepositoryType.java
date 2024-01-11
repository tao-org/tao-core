package ro.cs.tao.workspaces;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.LinkedHashMap;
import java.util.Map;

@XmlEnum(Integer.class)
public enum RepositoryType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local repository", "file", "root", null, null, "none",
          new LinkedHashMap<String, Boolean>(){{
              put("root", true);
    }}, true, false),
    @XmlEnumValue("2")
    AWS(2, "AWS S3 repository", "s3", "aws.bucket", null, null, "item",
        new LinkedHashMap<String, Boolean>() {{
            put("aws.region", true); put("aws.access.key", false);
            put("aws.secret.key", false); put("aws.bucket", true);
    }}, false, true),
    @XmlEnumValue("3")
    SWIFT(3, "OpenStack Swift S3 repository", "swift", "openstack.bucket", "openstack.user", "openstack.password", "item",
          new LinkedHashMap<String, Boolean>() {{
              put("openstack.auth.url", false); put("openstack.tenantId", false); put("openstack.domain", false);
              put("openstack.user", false); put("openstack.password", false); put("openstack.bucket", true);
    }}, false, true),
    @XmlEnumValue("4")
    STAC(4, "STAC repository", "stac", "stac.url", null, null, "position",
         new LinkedHashMap<String, Boolean>() {{
             put("stac.url", true); put("page.size", false);
    }}, false, true),
    @XmlEnumValue("5")
    SMB(5, "Network share repository", "smb", "smb.share", "smb.user", "smb.password", "none",
                 new LinkedHashMap<String, Boolean>() {{
        put("smb.server", true);
        put("smb.share", true);
        put("smb.domain", false);
        put("smb.user", false);
        put("smb.password", false);
    }}, false, true),
    @XmlEnumValue("6")
    FTP(6, "FTP repository", "ftp", "ftp.server", "ftp.user", "ftp.password", "none",
        new LinkedHashMap<String, Boolean>() {{
            put("ftp.server", true);
            put("ftp.user", false);
            put("ftp.password", false);
        }}, false, true),
    @XmlEnumValue("7")
    FTPS(7, "FTPS repository", "ftps", "ftps.server", "ftps.user", "ftps.password", "none",
        new LinkedHashMap<String, Boolean>() {{
            put("ftps.server", true);
            put("ftps.user", false);
            put("ftps.password", false);
        }}, false, true);
    private final int value;
    private final String description;
    private final String prefix;
    private final LinkedHashMap<String, Boolean> parameters;
    private final String rootKey;
    private final String userKey;
    private final String pwdKey;
    private final String pageItem;
    private final boolean singleton;
    private final boolean userVisible;

    RepositoryType(int value, String description, String prefix, String rootKey, String userKey, String pwdKey, String pageItem,
                   LinkedHashMap<String, Boolean> parameters, boolean singleton, boolean userVisible) {
        this.value = value;
        this.description = description;
        this.prefix = prefix;
        this.rootKey = rootKey;
        this.userKey = userKey;
        this.pwdKey = pwdKey;
        this.parameters = parameters;
        this.singleton = singleton;
        this.userVisible = userVisible;
        this.pageItem = pageItem;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    public String prefix() { return this.prefix; }

    public String rootKey() { return this.rootKey; }

    public String userKey() { return this.userKey; }

    public String passwordKey() { return this.pwdKey; }

    public String pageItem() { return this.pageItem; }

    public Map<String, Boolean> getParameters() { return new LinkedHashMap<>(parameters); }

    public boolean singleton() { return this.singleton; }

    public boolean visible() { return this.userVisible; }
    
    public static RepositoryType fromPrefix(String prefix) {
        for (RepositoryType type : RepositoryType.values()) {
            if (type.prefix.equals(prefix)) {
                return type;
            }
        }
        return null;
    }
}
