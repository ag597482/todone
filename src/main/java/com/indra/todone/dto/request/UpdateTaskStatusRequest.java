package com.indra.todone.dto.request;

import com.indra.todone.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskStatusRequest {

    private TaskStatus taskStatus;
    private String userId;
}
