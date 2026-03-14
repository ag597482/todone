package com.indra.todone.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Map;

@Document(collection = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @Field("task_id")
    @JsonProperty("task_id")
    private String taskId;
    private String name;
    private String description;
    private Map<String, Object> meta;
    private LocalDate dueDate;
    private LocalDate doneDate;
    private TaskStatus status;
    private String authorId;
}
