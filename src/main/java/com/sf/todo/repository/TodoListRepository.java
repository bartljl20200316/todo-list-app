package com.sf.todo.repository;

import com.sf.todo.model.TodoList;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoListRepository extends MongoRepository<TodoList, String>, CustomTodoListRepository {
    /**
     * Finds all lists where the given userId is either the owner
     * or is present in the collaborator set.
     *
     * @param userId1, userId2: The ID of the user.
     * @return A list of matching TodoLists.
     */
    List<TodoList> findByOwnerIdOrCollaborators_UserId(String userId1, String userId2);

}
