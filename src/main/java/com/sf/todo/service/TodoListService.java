package com.sf.todo.service;

import com.sf.todo.dto.*;
import com.sf.todo.exception.PermissionException;
import com.sf.todo.exception.ResourceNotFoundException;
import com.sf.todo.model.*;
import com.sf.todo.repository.TodoListRepository;
import com.sf.todo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TodoListService {

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    public TodoList createList(CreateTodoListDto dto, String ownerId) {
        TodoList newList = new TodoList();
        newList.setTitle(dto.getTitle());
        newList.setOwnerId(ownerId);

        return todoListRepository.save(newList);
    }

    /**
     * Finds all lists where the user is either the owner or a collaborator.
     */
    public List<TodoList> findListsForUser(String userId) {
        return todoListRepository.findByOwnerIdOrCollaborators_UserId(userId, userId);
    }

    /**
     * Adds list of new item to the todo_list.
     */
    public TodoList addItemToList(String listId, List<CreateTodoItemDto> itemDtoList, String currentUserId) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));

        // Permission Check
        if (!hasEditPermission(list, currentUserId)) {
            throw new PermissionException("You do not have permission to edit this list.");
        }

        List<TodoItem> newItemList = new ArrayList<>();
        for(CreateTodoItemDto itemDto : itemDtoList) {
            TodoItem newItem = new TodoItem();
            newItem.setId(UUID.randomUUID().toString());
            newItem.setName(itemDto.getName());
            newItem.setDescription(itemDto.getDescription());
            newItem.setDueDate(itemDto.getDueDate());
            newItem.setStatus(itemDto.getStatus());
            newItem.setPriority(itemDto.getPriority());
            newItem.setTags(itemDto.getTags());

            newItemList.add(newItem);
        }

        list.getItems().addAll(newItemList);
        TodoList updatedList = todoListRepository.save(list);

        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/lists/" + updatedList.getId(), updatedList);

        return updatedList;
    }

    /**
     * Update list of items in todo_list.
     */
    public TodoList updateItemInList(String listId, List<UpdateDeleteTodoItemDto> itemDtoUpdateList, String currentUserId) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));

        // Permission Check
        if (!hasEditPermission(list, currentUserId)) {
            throw new PermissionException("You do not have permission to edit this list.");
        }

        for(UpdateDeleteTodoItemDto updateDto : itemDtoUpdateList) {
            Optional<TodoItem> existedItem = list.getItems().stream().filter(item -> item.getId().equals(updateDto.getId())).findFirst();
            if(existedItem.isPresent()) {
                TodoItem item = existedItem.get();
                item.setName(updateDto.getName());
                item.setDescription(updateDto.getDescription());
                item.setDueDate(updateDto.getDueDate());
                item.setStatus(updateDto.getStatus());
                item.setPriority(updateDto.getPriority());
                item.setTags(updateDto.getTags());
            }
        }

        TodoList updatedList = todoListRepository.save(list);

        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/lists/" + updatedList.getId(), updatedList);

        return updatedList;
    }

    /**
     * Delete list of items in todo_list.
     */
    public TodoList deleteItemInList(String listId, List<UpdateDeleteTodoItemDto> itemDtoDeleteList, String currentUserId) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));

        // Permission Check
        if (!hasEditPermission(list, currentUserId)) {
            throw new PermissionException("You do not have permission to edit this list.");
        }

        for(UpdateDeleteTodoItemDto updateDto : itemDtoDeleteList) {
            Optional<TodoItem> existedItem = list.getItems().stream().filter(item -> item.getId().equals(updateDto.getId())).findFirst();
            existedItem.ifPresent(todoItem -> todoItem.setDeleted(Boolean.TRUE));
        }

        TodoList updatedList = todoListRepository.save(list);

        // Send real-time update via WebSocket
        messagingTemplate.convertAndSend("/topic/lists/" + updatedList.getId(), updatedList);

        return updatedList;
    }

    /**
     * Filter and sort todo_list items by status, due date, name.
     */
    public QueryResultDto<TodoItem> getFilteredItemsFromList(String listId, String name, ToDoStatus status, LocalDate dueDate, String currentUserId, String sortBy, String sortDir, Boolean includeDeleted) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));

        boolean hasViewPermission = list.getOwnerId().equals(currentUserId) ||
                list.getCollaborators().stream().anyMatch(c -> c.getUserId().equals(currentUserId));

        if (!hasViewPermission) {
            throw new PermissionException("You do not have permission to view this list.");
        }

        Map<String, Object> criteria = new HashMap<>();
        if (name != null) criteria.put("name", name);
        if (status != null) criteria.put("status", status);
        if (dueDate != null) criteria.put("dueDate", dueDate);
        if (!includeDeleted) criteria.put("deleted", Boolean.FALSE);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);

        return todoListRepository.findItemsByCriteria(listId, criteria, sort);
    }


    /**
     * Shares a todo_list with user.
     */
    public void shareList(String listId, ShareTodoListDto shareDto, String ownerId) {
        TodoList list = todoListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("List not found with id: " + listId));

        // Permission Check: Only the owner can share the list.
        if (!list.getOwnerId().equals(ownerId)) {
            throw new PermissionException("Only the owner can share this list.");
        }

        User userToShareWith = userRepository.findByEmail(shareDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + shareDto.getEmail()));

        Collaborator collaborator = new Collaborator();
        collaborator.setUserId(userToShareWith.getId());
        collaborator.setPermission(shareDto.getPermission());

        list.getCollaborators().add(collaborator);
        todoListRepository.save(list);
    }

    private boolean hasEditPermission(TodoList list, String userId) {
        if (list.getOwnerId().equals(userId)) {
            return true;
        }
        return list.getCollaborators().stream()
                .anyMatch(c -> c.getUserId().equals(userId) && c.getPermission() == Permission.EDIT);
    }
}