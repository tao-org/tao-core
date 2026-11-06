package ro.cs.tao.datasource.beans;

import ro.cs.tao.serialization.JsonMapper;

import java.util.Map;
import java.util.logging.Logger;

public class RepositoryQuery {

    private static final String SOURCE_REPOSITORY_KEY = "sourceRepositoryId";
    private static final String SOURCE_PATH_KEY = "sourcePath";
    private static final String DESTINATION_REPOSITORY_KEY = "destinationRepositoryId";
    private static final String DESTINATION_PATH_KEY = "destinationPath";
    private static final String ITEMS_COUNT_KEY = "itemsCount";
    private static final String FILE_NAME_PATTERN_KEY = "fileNamePattern";

    private String sourceRepositoryId;
    private String sourcePath;
    private String destinationRepositoryId;
    private String destinationPath;
    private Integer itemsCount;
    private String fileNamePattern;

    public String getSourceRepositoryId() {
        return sourceRepositoryId;
    }

    public void setSourceRepositoryId(String sourceRepositoryId) {
        this.sourceRepositoryId = sourceRepositoryId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getDestinationRepositoryId() {
        return destinationRepositoryId;
    }

    public void setDestinationRepositoryId(String destinationRepositoryId) {
        this.destinationRepositoryId = destinationRepositoryId;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getFileNamePattern() {
        return fileNamePattern;
    }

    public void setFileNamePattern(String fileNamePattern) {
        this.fileNamePattern = fileNamePattern;
    }

    @Override
    public String toString() {
        try {
            return JsonMapper.instance().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(RepositoryQuery.class.getName()).severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static RepositoryQuery fromMap(Map<String, String> map) {
        if (map == null) return null;
        final RepositoryQuery query = new RepositoryQuery();
        query.sourceRepositoryId = map.get(SOURCE_REPOSITORY_KEY);
        query.sourcePath = map.get(SOURCE_PATH_KEY);
        query.destinationRepositoryId = map.get(DESTINATION_REPOSITORY_KEY);
        query.destinationPath = map.get(DESTINATION_PATH_KEY);
        if (map.containsKey(ITEMS_COUNT_KEY)) {
            query.itemsCount = Integer.parseInt(ITEMS_COUNT_KEY);
        }
        query.fileNamePattern = map.get(FILE_NAME_PATTERN_KEY);
        return query;
    }

    public static RepositoryQuery fromJSON(String json) {
        try {
            return JsonMapper.instance().readValue(json, RepositoryQuery.class);
        } catch (Exception ex) {
            Logger.getLogger(RepositoryQuery.class.getName()).severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
