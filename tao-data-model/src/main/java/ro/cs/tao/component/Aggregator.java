package ro.cs.tao.component;

import java.util.Arrays;

public class Aggregator {
    private String[] sorter;
    private String[] associator;

    public String[] getSorter() { return sorter; }
    public void setSorter(String[] sorter) { this.sorter = sorter; }

    public String[] getAssociator() { return associator; }
    public void setAssociator(String[] associator) { this.associator = associator; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregator that = (Aggregator) o;
        return Arrays.equals(sorter, that.sorter) &&
                Arrays.equals(associator, that.associator);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sorter);
        result = 31 * result + Arrays.hashCode(associator);
        return result;
    }
}
