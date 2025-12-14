package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {

    private Boolean success;

    private String message;

    private Instant timestamp;

    private Object data;

    public static SuccessResponse of(String message) {
        return SuccessResponse.builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static SuccessResponse of(String message, Object data) {
        return SuccessResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }
}
