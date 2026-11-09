package ro.cs.tao.topology.docker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents a response fetched from Docker Hub, typically obtained from an API call.
 * This class contains metadata about the paginated response, including:
 * - The size of each page in the results.
 * - Links to the next and previous pages of the result set.
 * - The current page number.
 * - The total count of records available.
 * Additionally, it includes a list of DockerHubRecord objects representing the individual summaries
 * or items in the response.
 */
public class DockerHubResponse {
    private int pageSize;
    private String next;
    private String previous;
    private int page;
    private int count;
    List<DockerHubRecord> summaries;

    @JsonProperty("page_size")
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<DockerHubRecord> getSummaries() {
        return summaries;
    }

    public void setSummaries(List<DockerHubRecord> summaries) {
        this.summaries = summaries;
    }
}
