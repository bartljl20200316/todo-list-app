package com.sf.todo.dto;

import lombok.Data;
import java.util.List;

@Data
public class QueryResultDto<T> {

    private Integer totalCount;
    private List<T> results;

    public QueryResultDto(Integer totalCount, List<T> results) {
        this.totalCount = totalCount;
        this.results = results;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
