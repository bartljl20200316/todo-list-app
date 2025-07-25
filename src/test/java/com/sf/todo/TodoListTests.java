package com.sf.todo;

import com.sf.todo.dto.CreateTodoItemDto;
import com.sf.todo.dto.CreateTodoListDto;
import com.sf.todo.exception.PermissionException;
import com.sf.todo.exception.ResourceNotFoundException;
import com.sf.todo.model.*;
import com.sf.todo.repository.CustomTodoListRepositoryImpl;
import com.sf.todo.repository.TodoListRepository;
import com.sf.todo.service.TodoListService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoListTests {

	@Mock
	private TodoListRepository todoListRepository;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private MongoTemplate mongoTemplate;

	@InjectMocks
	private CustomTodoListRepositoryImpl customTodoListRepository;

	@InjectMocks
	private TodoListService todoListService;

	private TodoList testList;
	private List<CreateTodoItemDto> newItemDtoList;

	private final String ownerId = "user-owner";
    private final String listId = "list-123";
	private final String otherUserId = "user-other";

	@BeforeEach
	void setUp() {
		testList = new TodoList();
		testList.setId(listId);
		testList.setOwnerId(ownerId);

		Collaborator collaborator = new Collaborator();
        String collaboratorId = "user-collaborator";
        collaborator.setUserId(collaboratorId);
		collaborator.setPermission(Permission.EDIT);

		Set<Collaborator> collaborators = new HashSet<>();
		collaborators.add(collaborator);
		testList.setCollaborators(collaborators);

		newItemDtoList = new ArrayList<>();
		CreateTodoItemDto newItemDto = new CreateTodoItemDto();
		newItemDto.setName("New Test Item");
		newItemDtoList.add(newItemDto);
	}

	@Test
	void createList_ShouldSaveAndReturnList() {
		CreateTodoListDto dto = new CreateTodoListDto();
        String ownerId = "user-123";
        String title = "Test List";
		dto.setTitle(title);

		when(todoListRepository.save(any(TodoList.class))).thenAnswer(invocation -> invocation.getArgument(0));

		TodoList result = todoListService.createList(dto, ownerId);

		assertNotNull(result);
		assertEquals(title, result.getTitle());
		assertEquals(ownerId, result.getOwnerId());
		verify(todoListRepository, times(1)).save(any(TodoList.class));
	}

	@Test
	void addItemToList_Success_WhenUserIsOwner() {
		when(todoListRepository.findById(listId)).thenReturn(Optional.of(testList));
		when(todoListRepository.save(any(TodoList.class))).thenReturn(testList);

		TodoList updatedList = todoListService.addItemToList(listId, newItemDtoList, ownerId);

		assertNotNull(updatedList);
		assertEquals(1, updatedList.getItems().size());
		assertEquals("New Test Item", updatedList.getItems().get(0).getName());

		verify(todoListRepository, times(1)).save(testList);
		verify(messagingTemplate, times(1)).convertAndSend("/topic/lists/" + listId, testList);
	}

	@Test
	void addItemToList_ThrowsAccessDenied_WhenUserLacksPermission() {
		when(todoListRepository.findById(listId)).thenReturn(Optional.of(testList));

		assertThrows(PermissionException.class, () -> {
			todoListService.addItemToList(listId, newItemDtoList, otherUserId);
		});

		verify(todoListRepository, never()).save(any());
		verify(messagingTemplate, never()).convertAndSend("/topic/lists/" + listId, testList);
	}

	@Test
	void addItemToList_ThrowsRuntimeException_WhenListNotFound() {
		String nonExistentListId = "list-404";
		when(todoListRepository.findById(nonExistentListId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			todoListService.addItemToList(nonExistentListId, newItemDtoList, ownerId);
		});
	}

	@Test
	@Disabled
	void findItemsByCriteria_WithSingleFilterAndSort_BuildsCorrectAggregation() {
		String listId = "list-1";
		Map<String, Object> criteria = new HashMap<>();
		criteria.put("status", ToDoStatus.IN_PROGRESS);
		Sort sort = Sort.by(Sort.Direction.DESC, "dueDate");

		ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);

		customTodoListRepository.findItemsByCriteria(listId, criteria, sort);

		verify(mongoTemplate).aggregate(aggregationCaptor.capture(), eq("todolist"), eq(TodoItem.class));
		Aggregation capturedAggregation = aggregationCaptor.getValue();
		String aggregationString = capturedAggregation.toString();

		assertTrue(aggregationString.contains("\"$match\": { \"_id\": \"list-1\" }"));
		assertTrue(aggregationString.contains("\"$unwind\": \"$items\""));
		assertTrue(aggregationString.contains("\"$match\": { \"items.status\": \"IN_PROGRESS\" }"));
		assertTrue(aggregationString.contains("\"$sort\": { \"dueDate\": -1 }"));
		assertTrue(aggregationString.contains("\"$replaceRoot\": { \"newRoot\": \"$items\" }"));
	}

	@Test
	@Disabled
	void findItemsByCriteria_WithMultipleFilters_BuildsCorrectAggregation() {
		String listId = "list-2";
		Map<String, Object> criteria = new HashMap<>();
		criteria.put("status", ToDoStatus.COMPLETED);
		criteria.put("dueDate", LocalDate.of(2025, 1, 1));
		Sort sort = Sort.by(Sort.Direction.ASC, "name");

		ArgumentCaptor<Aggregation> aggregationCaptor = ArgumentCaptor.forClass(Aggregation.class);

		customTodoListRepository.findItemsByCriteria(listId, criteria, sort);

		verify(mongoTemplate).aggregate(aggregationCaptor.capture(), anyString(), any());
		String aggregationString = aggregationCaptor.getValue().toString();

		assertTrue(aggregationString.contains("\"$match\": { \"items.status\": \"COMPLETED\", \"items.dueDate\": { \"$date\": \"2025-01-01T00:00:00.000Z\" } }"));
		assertTrue(aggregationString.contains("\"$sort\": { \"name\": 1 }"));
	}


	@Test
	void getFilteredItemsFromList_Success_WhenUserHasPermission() {
		when(todoListRepository.findById(listId)).thenReturn(Optional.of(testList));

		todoListService.getFilteredItemsFromList(listId, "Test Item", ToDoStatus.NOT_STARTED, LocalDate.now(), ownerId, "name", "ASC", Boolean.TRUE);

		verify(todoListRepository, times(1)).findItemsByCriteria(eq(listId), anyMap(), any(Sort.class));
	}

}
