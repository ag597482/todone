package com.indra.todone.repository;

import com.indra.todone.model.Task;
import com.indra.todone.model.TaskStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByAuthorId(String authorId);

    List<Task> findByAuthorIdAndDueDate(String authorId, LocalDate dueDate);

    List<Task> findByAuthorIdAndTaskGroupId(String authorId, String taskGroupId);

    List<Task> findByAuthorIdAndStatusAndDueDateLessThanEqual(String authorId, TaskStatus status, LocalDate dueDate);

    List<Task> findByAuthorIdAndStatusAndDueDate(String authorId, TaskStatus status, LocalDate dueDate);
}
