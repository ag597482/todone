package com.indra.todone.repository;

import com.indra.todone.model.TaskGroup;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskGroupRepository extends MongoRepository<TaskGroup, String> {

    List<TaskGroup> findByAuthorIdOrderByNameAsc(String authorId);
}

