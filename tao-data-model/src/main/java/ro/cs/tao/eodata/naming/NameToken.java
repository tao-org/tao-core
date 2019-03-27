package ro.cs.tao.eodata.naming;

public class NameToken {
    private String name;
    private String description;
    private int matchingGroupNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMatchingGroupNumber() {
        return matchingGroupNumber;
    }

    public void setMatchingGroupNumber(int value) {
        this.matchingGroupNumber = value;
    }
}
