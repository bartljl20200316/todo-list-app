package com.sf.todo.dto;

import com.sf.todo.model.ToDoPriority;
import com.sf.todo.model.ToDoStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateDeleteTodoItemDto {

    private String id;
    private String name;
    private String description;
    private LocalDate dueDate;
    private ToDoStatus status;
    private ToDoPriority priority;
    private List<String> tags;
    private boolean deleted;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ToDoStatus getStatus() {
        return status;
    }

    public void setStatus(ToDoStatus status) {
        this.status = status;
    }

    public ToDoPriority getPriority() {
        return priority;
    }

    public void setPriority(ToDoPriority priority) {
        this.priority = priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isDeleted() { return deleted; }

    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
