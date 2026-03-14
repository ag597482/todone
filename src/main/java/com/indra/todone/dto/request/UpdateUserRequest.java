package com.indra.todone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    private String name;
    private Map<String, Object> metadata;
}
