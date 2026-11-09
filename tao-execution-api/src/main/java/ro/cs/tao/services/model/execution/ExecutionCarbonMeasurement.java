package ro.cs.tao.services.model.execution;

public class ExecutionCarbonMeasurement {
    private String name;
    private Long size;

    private String url;
    private String relativePath;

    private String repositoryId;

    public ExecutionCarbonMeasurement(String name, Long size, String url, String relativePath, String repositoryId) {
        this.name = name;
        this.size = size;
        this.url = url;
        this.relativePath = relativePath;
        this.repositoryId = repositoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long value) {
        this.size = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }
}
