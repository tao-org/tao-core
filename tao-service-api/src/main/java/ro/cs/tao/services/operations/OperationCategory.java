package ro.cs.tao.services.operations;

import ro.cs.tao.TaoEnum;

/**
 * Represents a category of operations that can be called by a LLM.
 * The description of the category must match the UI fragment for which operations are intended.
 *
 * @author  Cosmin Cara
 * @since   2.0.0
 */
public enum OperationCategory implements TaoEnum<Integer> {
    WORKFLOW(1, "workflows-admin2"),
    REPOSITORY(2, "repositories"),
    DATASOURCE(3, "datasources-queries"),
    EXECUTION(4, "dashboard-admin"),
    SITE(5, "sites");

    private final int value;
    private final String description;

    OperationCategory(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Integer value() {
        return this.value;
    }

    @Override
    public boolean isVisible() {
        return false;
    }


}
