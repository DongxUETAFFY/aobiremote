package io.github.dongxuetaffy.aobihelper.publiczone.dto;

import jakarta.validation.constraints.Size;

public class PublicPostToggleUntrustedRequest {
    @Size(max = 128)
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
