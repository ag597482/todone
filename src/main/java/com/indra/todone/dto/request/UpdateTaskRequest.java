package com.indra.todone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import com.indra.todone.model.TaskStep;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskRequest {

    private String name;
    private String description;
    /**
     * Full list of subtasks (steps) to set on the task.
     * If null, existing steps are left unchanged.
     */
    private List<TaskStep> steps;
    /**
     * Optional meta map, primarily used to accept {"steps": [...]} from clients.
     * If provided and contains a "steps" key, it will be used to update the task steps.
     */
    private Map<String, Object> meta;
    /** Task time stored in meta under key "time" (e.g. "14:30" or "14:30:00"). */
    private String time;
    /**
     * ID of the user performing the update; must match authorId.
     */
    private String userId;
}

