package ro.cs.tao.eodata.naming;

import java.util.List;

public class NamingRule {
    private int id;
    private String sensor;
    private String regEx;
    private String description;
    private List<NameToken> tokens;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getRegEx() {
        return regEx;
    }

    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<NameToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<NameToken> tokens) {
        this.tokens = tokens;
    }
}
