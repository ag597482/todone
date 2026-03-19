package com.indra.todone.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskRequest {

    private String name;
    private String description;
    private Map<String, Object> meta;
    /** Task time stored in meta under key "time" (e.g. "14:30" or "14:30:00"). */
    private String time;
    /** Optional task group id this task belongs to. Can be null. */
    @JsonProperty("task_group_id")
    @JsonAlias({"taskGroupId"})
    private String taskGroupId;
    private LocalDate dueDate;
    private String authorId;
}
