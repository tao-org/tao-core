package ro.cs.tao.topology.docker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
