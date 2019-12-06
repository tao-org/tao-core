package ro.cs.tao.datasource.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CliDescriptor {
    private List<CliOption> options;
    private List<CliOptionGroup> groups;

    public List<CliOption> getOptions() {
        return options;
    }

    public void setOptions(List<CliOption> options) {
        this.options = options;
    }

    public List<CliOptionGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<CliOptionGroup> groups) {
        this.groups = groups;
    }

    public List<CliOption> findGroupOptions(String groupName) {
        List<CliOption> options = null;
        if (this.groups != null && this.options != null) {
            final CliOptionGroup group = this.groups.stream().filter(g -> g.getName().equals(groupName)).findFirst().orElse(null);
            if (group == null) {
                throw new IllegalArgumentException(String.format("Invalid option group '%s'", groupName));
            }
            final Set<String> gOpts = group.getOptions();
            options = this.options.stream().filter(o -> gOpts.contains(o.getName())).collect(Collectors.toList());
        }
        return options;
    }

    public List<CliOption> findNonGroupedOptions() {
        List<CliOption> options = null;
        if (this.options != null) {
            options = new ArrayList<>(this.options);
            if (this.groups != null) {
                for (CliOptionGroup group : this.groups) {
                    final Set<String> set = group.getOptions();
                    if (set != null) {
                        options.removeIf(o -> set.contains(o.getName()));
                    }
                }
            }
        }
        return options;
    }
}
