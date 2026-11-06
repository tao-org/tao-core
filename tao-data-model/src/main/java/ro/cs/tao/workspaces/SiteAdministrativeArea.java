package ro.cs.tao.workspaces;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SiteAdministrativeArea {
    private String country;
    private Map<String, List<String>> adml1;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Map<String, List<String>> getAdml1() {
        return adml1;
    }

    public void setAdml1(Map<String, List<String>> adml1) {
        this.adml1 = adml1;
    }

    public boolean isEmpty() {
        return country == null || country.trim().isEmpty();
    }

    public void addAdminL1(String adminL1Id, List<String> adminL2Ids) {
        if (this.adml1 == null) {
            this.adml1 = new LinkedHashMap<>();
        }
        this.adml1.put(adminL1Id, adminL2Ids);
    }
}
