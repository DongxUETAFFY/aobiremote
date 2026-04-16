package io.github.dongxuetaffy.aobihelper.feedback.controller;

import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.common.controller.PlaceholderControllerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController extends PlaceholderControllerSupport {

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitFeedback() {
        return notImplemented("Feedback submission is not implemented yet");
    }
}
