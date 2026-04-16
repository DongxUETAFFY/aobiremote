package io.github.dongxuetaffy.aobihelper.inventory.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class InventoryBatchDeleteRequest {
    @NotEmpty(message = "ids cannot be empty")
    private List<@NotNull(message = "id cannot be null") Long> ids;

    private String requestId;

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
