package com.htv.oauth2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@RegisterForReflection
public class MfaSetupResponse {

    @JsonProperty("mfa_secret")
    private String mfaSecret;

    @JsonProperty("qr_code")
    private String qrCode; // Base64 encoded QR code image

    @JsonProperty("manual_entry_key")
    private String manualEntryKey;

    @JsonProperty("backup_codes")
    private List<String> backupCodes;
}
