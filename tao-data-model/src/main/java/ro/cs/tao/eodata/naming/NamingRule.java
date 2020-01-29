package ro.cs.tao.eodata.naming;

import ro.cs.tao.component.Identifiable;

import java.util.List;

public class NamingRule implements Identifiable<Integer> {
    private int id;
    private String sensor;
    private String regEx;
    private String description;
    private List<NameToken> tokens;

    @Override
    public Integer getId() {
        return id;
    }
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
    @Override
    public Integer defaultId() { return 0; }

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
