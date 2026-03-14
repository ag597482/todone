package com.indra.todone.repository;

import com.indra.todone.model.Routine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRepository extends MongoRepository<Routine, String> {

    List<Routine> findByAuthorId(String authorId);
}
