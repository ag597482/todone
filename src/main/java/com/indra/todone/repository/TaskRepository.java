package com.indra.todone.repository;

import com.indra.todone.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    List<Task> findByAuthorId(String authorId);

    List<Task> findByAuthorIdAndDueDate(String authorId, LocalDate dueDate);
}
