package com.sf.todo.controller;

import com.sf.todo.dto.*;
import com.sf.todo.model.ToDoStatus;
import com.sf.todo.model.TodoItem;
import com.sf.todo.model.TodoList;
import com.sf.todo.model.User;
import com.sf.todo.service.TodoListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/todo-list")
public class TodoListController {

    @Autowired
    private TodoListService todoListService;

    /**
     * Creates a new TodoList.
     * The owner is automatically set to the currently authenticated user.
     *
     * @param createListDto DTO containing the title of the new list.
     * @param authentication The security context to identify the current user.
     * @return The newly created TodoList.
     */
    @PostMapping
    public ResponseEntity<TodoList> createTodoList(@RequestBody CreateTodoListDto createListDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TodoList createdList = todoListService.createList(createListDto, currentUser.getId());
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    /**
     * Gets all lists owned by or shared with the current user.
     *
     * @param authentication The security context to identify the current user.
     * @return A list of TodoLists.
     */
    @GetMapping
    public ResponseEntity<QueryResultDto<TodoList>> getUserLists(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<TodoList> todoList = todoListService.findListsForUser(currentUser.getId());
        return ResponseEntity.ok(new QueryResultDto<>(todoList.size(), todoList));
    }

    /**
     * Adds list of new items to a specific todo_list.
     *
     * @param listId The ID of the list to add the item to.
     * @param itemDtoList DTO containing the details of the new item list.
     * @param authentication The security context for permission checking.
     * @return The updated TodoList.
     */
    @PostMapping("/{listId}/items")
    public ResponseEntity<TodoList> addItemToList(@PathVariable String listId, @RequestBody List<CreateTodoItemDto> itemDtoList, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TodoList updatedList = todoListService.addItemToList(listId, itemDtoList, currentUser.getId());
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Update list of new items in a specific todo_list.
     *
     * @param listId The ID of the list to add the item to.
     * @param itemDtoList DTO containing the details of the updated item list.
     * @param authentication The security context for permission checking.
     * @return The updated TodoList.
     */
    @PutMapping("/{listId}/items")
    public ResponseEntity<TodoList> updateItemInList(@PathVariable String listId, @RequestBody List<UpdateDeleteTodoItemDto> itemDtoList, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TodoList updatedList = todoListService.updateItemInList(listId, itemDtoList, currentUser.getId());
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Delete list of items in a specific todo_list.
     *
     * @param listId The ID of the list to add the item to.
     * @param itemDtoList DTO containing the details of the deleted item list.
     * @param authentication The security context for permission checking.
     * @return The updated TodoList.
     */
    @PutMapping("/{listId}/items-delete")
    public ResponseEntity<TodoList> deleteItemInList(@PathVariable String listId, @RequestBody List<UpdateDeleteTodoItemDto> itemDtoList, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        TodoList updatedList = todoListService.deleteItemInList(listId, itemDtoList, currentUser.getId());
        return ResponseEntity.ok(updatedList);
    }

    /**
     * Gets items from a list with optional filtering and sorting.
     *
     * @param listId The ID of the list.
     * @param name Optional filter by item name.
     * @param status Optional filter by item status.
     * @param dueDate Optional filter by item due date.
     * @param includeDeleted Optional filter by including deleted item.
     * @param sortBy Field to sort by (e.g., 'dueDate', 'name').
     * @param sortDir Sort direction ('ASC' or 'DESC').
     * @param authentication The security context for permission checking.
     * @return A list of matching TodoItems.
     */
    @GetMapping("/{listId}/items")
    public ResponseEntity<QueryResultDto<TodoItem>> getFilteredAndSortedItems(
            @PathVariable String listId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ToDoStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
            @RequestParam(defaultValue = "dueDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir,
            @RequestParam(defaultValue = "true") Boolean includeDeleted,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        QueryResultDto<TodoItem> items = todoListService.getFilteredItemsFromList(listId, name, status, dueDate, currentUser.getId(), sortBy, sortDir, includeDeleted);
        return ResponseEntity.ok(items);
    }

    /**
     * Shares a list with another user.
     * Only the owner of the list can perform this action.
     *
     * @param listId The ID of the list to share.
     * @param shareDto DTO containing the recipient's email and permission level.
     * @param authentication The security context to identify the owner.
     * @return A response entity indicating success.
     */
    @PostMapping("/{listId}/share")
    public ResponseEntity<Void> shareList(@PathVariable String listId, @RequestBody ShareTodoListDto shareDto, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        todoListService.shareList(listId, shareDto, currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
