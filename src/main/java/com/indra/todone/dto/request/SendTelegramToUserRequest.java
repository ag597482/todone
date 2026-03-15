package com.indra.todone.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendTelegramToUserRequest {

    private String userId;
    private String phoneNumber;
    private String text;
}
