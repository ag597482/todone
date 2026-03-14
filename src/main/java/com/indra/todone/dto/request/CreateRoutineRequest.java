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
public class CreateRoutineRequest {

    private String name;
    private String desc;
    private Map<String, Object> meta;
    private int repeatFrequencyDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private String authorId;
}
