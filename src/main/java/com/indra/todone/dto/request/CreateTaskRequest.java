package com.indra.todone.dto.request;

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
    private LocalDate dueDate;
    private String authorId;
}
