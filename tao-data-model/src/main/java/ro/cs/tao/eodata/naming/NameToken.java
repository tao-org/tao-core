package ro.cs.tao.eodata.naming;

/**
 * Descriptor of a name token that is part of a naming rule.
 *
 * @author Cosmin Cara
 */
public class NameToken {
    private String name;
    private String description;
    private int matchingGroupNumber;

    /**
     * Returns the name of this token
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of this token
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the position of this token in the naming rule expression
     */
    public int getMatchingGroupNumber() {
        return matchingGroupNumber;
    }

    public void setMatchingGroupNumber(int value) {
        this.matchingGroupNumber = value;
    }
}
