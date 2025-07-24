package com.sf.todo.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
public class TodoItem {

    private String id;
    private String name;
    private String description;
    private LocalDate dueDate;
    private ToDoStatus status = ToDoStatus.NOT_STARTED;
    private ToDoPriority priority = ToDoPriority.MEDIUM;
    private List<String> tags;
    private boolean deleted = false;

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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TodoItem item = (TodoItem) o;
        return deleted == item.deleted && Objects.equals(id, item.id) && Objects.equals(name, item.name) && Objects.equals(description, item.description) && Objects.equals(dueDate, item.dueDate) && status == item.status && priority == item.priority && Objects.equals(tags, item.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, dueDate, status, priority, tags, deleted);
    }
}
