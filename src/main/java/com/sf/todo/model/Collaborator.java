package com.sf.todo.model;

import lombok.Data;

import java.util.Objects;

@Data
public class Collaborator {

    private String userId;
    private Permission permission;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Collaborator that = (Collaborator) o;
        return Objects.equals(userId, that.userId) && permission == that.permission;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, permission);
    }
}
