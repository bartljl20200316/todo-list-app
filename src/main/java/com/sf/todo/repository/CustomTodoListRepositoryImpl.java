package com.sf.todo.repository;

import com.sf.todo.dto.QueryResultDto;
import com.sf.todo.model.TodoItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomTodoListRepositoryImpl implements CustomTodoListRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public QueryResultDto<TodoItem> findItemsByCriteria(String listId, Map<String, Object> criteriaMap, Sort sort) {
        List<AggregationOperation> operations = new ArrayList<>();
        operations.add(Aggregation.match(Criteria.where("_id").is(listId)));
        operations.add(Aggregation.unwind("items"));

        Criteria filterCriteria = new Criteria();
        for (Map.Entry<String, Object> entry : criteriaMap.entrySet()) {
            filterCriteria.and("items." + entry.getKey()).is(entry.getValue());
        }

        MatchOperation filterOperation = Aggregation.match(filterCriteria);
        operations.add(filterOperation);

        Sort modifiedSort = Sort.by(
                sort.stream()
                        .map(order -> new Sort.Order(order.getDirection(), "items." + order.getProperty()))
                        .collect(Collectors.toList())
        );

        operations.add(Aggregation.sort(modifiedSort));
        operations.add(Aggregation.replaceRoot("items"));

        Aggregation aggregation = Aggregation.newAggregation(operations);
        List<TodoItem> todoItemList = mongoTemplate.aggregate(aggregation, "todolist", TodoItem.class).getMappedResults();

        return new QueryResultDto<>(todoItemList.size(), todoItemList);
    }
}
