package com.indra.todone.dto.response;

import com.indra.todone.model.Task;
import com.indra.todone.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {

    private User user;
    private List<Task> tasks;
    private long completedTasksCount;
    private long pendingTasksCount;
}
