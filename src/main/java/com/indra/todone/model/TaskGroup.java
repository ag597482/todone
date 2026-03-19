package com.indra.todone.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "task_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskGroup {

    @Id
    @Field("task_group_id")
    @JsonProperty("task_group_id")
    private String taskGroupId;

    private String name;

    /** User who created/owns this group; serialized as user_id in JSON. */
    @JsonProperty("user_id")
    @JsonAlias({"authorId", "author_id"})
    private String authorId;
}

