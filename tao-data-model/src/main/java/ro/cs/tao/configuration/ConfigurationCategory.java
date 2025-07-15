package ro.cs.tao.configuration;

/**
 * Represents a category of configuration items used for grouping related configurations.
 * Each category is defined by an identifier, a name, and an order, which specifies its placement
 * in a predefined sequence.
 */
public class ConfigurationCategory {
    private int id;
    private String name;
    private int order;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
