package io.github.dongxuetaffy.aobihelper.common.controller;

import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class PlaceholderControllerSupport {

    protected ResponseEntity<ApiResponse<Void>> notImplemented(String message) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(ApiResponse.fail(50100, message));
    }
}
