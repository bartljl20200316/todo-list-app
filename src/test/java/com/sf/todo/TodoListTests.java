package com.sf.todo;

import com.sf.todo.dto.CreateTodoListDto;
import com.sf.todo.model.TodoList;
import com.sf.todo.repository.TodoListRepository;
import com.sf.todo.service.TodoListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoListTests {

	@Mock
	private TodoListRepository todoListRepository;

	@InjectMocks
	private TodoListService todoListService;

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


}
