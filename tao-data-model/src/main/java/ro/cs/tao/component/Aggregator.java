package ro.cs.tao.component;

import java.util.Arrays;
import java.util.Objects;

/**
 * Holder for lists of sorter, association and filter names.
 *
 * @author Cosmin Cara
 */
public class Aggregator {
    private String[] sorter;
    private String[] associator;
    private String[] filter;
    private Integer order;

    /**
     * Retrieves the array of sorter names.
     *
     * @return an array of strings representing the sorter names
     */
    public String[] getSorter() { return sorter; }
    public void setSorter(String[] sorter) { this.sorter = sorter; }

    /**
     * Retrieves the array of associator names.
     *
     * @return an array of strings representing the associator names
     */
    public String[] getAssociator() { return associator; }
    public void setAssociator(String[] associator) { this.associator = associator; }

    /**
     * Retrieves the array of filter names.
     *
     * @return an array of strings representing the filter names
     */
    public String[] getFilter() { return filter; }
    public void setFilter(String[] filter) { this.filter = filter; }

    /**
     * Retrieves the order value.
     *
     * @return an integer representing the order value
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the order value.
     *
     * @param order the integer value representing the order to be set
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aggregator that = (Aggregator) o;
        return Arrays.equals(sorter, that.sorter)
               && Arrays.equals(associator, that.associator)
               && Arrays.equals(filter, that.filter)
               && order == that.order;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(sorter);
        result = 31 * result + Arrays.hashCode(associator);
        result = 31 * result + Arrays.hashCode(filter);
        result = 31 * result + Objects.hashCode(order);
        return result;
    }
}
