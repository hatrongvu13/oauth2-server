package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientListResponse {

    private List<ClientSummary> clients;

    private Long total;

    private Integer page;

    private Integer size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientSummary {
        @JsonProperty("client_id")
        private String clientId;

        @JsonProperty("client_name")
        private String clientName;

        private String description;

        private Boolean enabled;

        @JsonProperty("created_at")
        private Instant createdAt;
    }
}
