package ro.cs.tao.utils;

public enum ExecutionUnitFormat {
    /**
     * Execution unit should be executed by TAO
     */
    TAO(null),
    /**
     * Execution unit should be converted to JSON
     */
    JSON(".json"),
    /**
     * Execution unit should be converted to CWL
     */
    CWL(".cwl"),
    /**
     * Execution unit should be converted to Bash shell script
     */
    BASH(".sh"),
    /**
     * Execution unit should be converted to an Argo workflow
     */
    ARGO(".yaml");

    private final String extension;

    ExecutionUnitFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
