package com.sf.todo.repository;

import com.sf.todo.dto.QueryResultDto;
import com.sf.todo.model.TodoItem;
import org.springframework.data.domain.Sort;
import java.util.Map;

public interface CustomTodoListRepository {
    QueryResultDto<TodoItem> findItemsByCriteria(String listId, Map<String, Object> criteria, Sort sort);
}