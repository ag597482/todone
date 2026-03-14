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
public class CreateUserRequest {

    private String name;
    private String phoneNumber;
    private Map<String, Object> metadata;
}
