package ro.cs.tao.topology.docker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class DockerHubRecord {
    private String id;
    private String name;
    private String slug;
    private String type;
    private Object publisher;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String shortDescription;
    private String source;
    private boolean extensionReviewed;
    private long popularity;
    private Object categories;
    private Object operatingSystems;
    private Object architectures;
    private String logoURL;
    private String certificationStatus;
    private int starCount;
    private String filterType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getPublisher() {
        return publisher;
    }

    public void setPublisher(Object publisher) {
        this.publisher = publisher;
    }

    @JsonProperty("created_at")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("updated_at")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("short_description")
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @JsonProperty("extension_reviewed")
    public boolean isExtensionReviewed() {
        return extensionReviewed;
    }

    public void setExtensionReviewed(boolean extensionReviewed) {
        this.extensionReviewed = extensionReviewed;
    }

    public long getPopularity() {
        return popularity;
    }

    public void setPopularity(long popularity) {
        this.popularity = popularity;
    }

    public Object getCategories() {
        return categories;
    }

    public void setCategories(Object categories) {
        this.categories = categories;
    }

    @JsonProperty("operating_systems")
    public Object getOperatingSystems() {
        return operatingSystems;
    }

    public void setOperatingSystems(Object operatingSystems) {
        this.operatingSystems = operatingSystems;
    }

    public Object getArchitectures() {
        return architectures;
    }

    public void setArchitectures(Object architectures) {
        this.architectures = architectures;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    @JsonProperty("certification_status")
    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    @JsonProperty("star_count")
    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
}
