package ro.cs.tao.component;

import java.util.Arrays;

/**
 * Holder for lists of sorter, association and filter names.
 *
 * @author Cosmin Cara
 */
public class Aggregator {
    private String[] sorter;
    private String[] associator;
    private String[] filter;

    public String[] getSorter() { return sorter; }
    public void setSorter(String[] sorter) { this.sorter = sorter; }

    public String[] getAssociator() { return associator; }
    public void setAssociator(String[] associator) { this.associator = associator; }

    public String[] getFilter() { return filter; }
    public void setFilter(String[] filter) { this.filter = filter; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregator that = (Aggregator) o;
        return Arrays.equals(sorter, that.sorter) && Arrays.equals(associator, that.associator) && Arrays.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sorter);
        result = 31 * result + Arrays.hashCode(associator);
        result = 31 * result + Arrays.hashCode(filter);
        return result;
    }
}
