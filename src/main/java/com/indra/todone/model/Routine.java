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

@Document(collection = "routines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {

    @Id
    @Field("routine_id")
    @JsonProperty("routine_id")
    private String routineId;
    private String name;
    private String desc;
    private Map<String, Object> meta;
    private int repeatFrequencyDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isExecuted;
    private String authorId;
}
