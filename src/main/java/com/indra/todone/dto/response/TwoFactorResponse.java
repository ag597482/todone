package com.indra.todone.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TwoFactorResponse {

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Details")
    private String details;
}
