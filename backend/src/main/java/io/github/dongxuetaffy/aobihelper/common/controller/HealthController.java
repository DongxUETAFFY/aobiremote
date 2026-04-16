package io.github.dongxuetaffy.aobihelper.common.controller;

import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
            "service", "aobihelper-backend",
            "status", "UP"
        ));
    }
}
