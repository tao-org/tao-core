package ro.cs.tao.datasource.cli;

import java.util.Set;

public class CliOptionGroup {
    private String name;
    private Set<String> options;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getOptions() {
        return options;
    }

    public void setOptions(Set<String> options) {
        this.options = options;
    }
}
