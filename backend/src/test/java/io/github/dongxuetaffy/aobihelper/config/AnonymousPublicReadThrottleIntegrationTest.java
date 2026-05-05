package io.github.dongxuetaffy.aobihelper.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.dongxuetaffy.aobihelper.BackendApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = BackendApplication.class,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:anonymous_public_throttle_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false",
        "app.public-access.page-limit=2",
        "app.public-access.page-window-seconds=60",
        "app.public-access.detail-limit=1",
        "app.public-access.detail-window-seconds=60",
        "app.public-access.preview-limit=1",
        "app.public-access.preview-window-seconds=60"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("local-lite")
class AnonymousPublicReadThrottleIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousPublicPageRequestsAreThrottledAfterLimit() throws Exception {
        mockMvc.perform(get("/api/public-post/page"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-post/page"))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/public-post/page"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(1006))
            .andExpect(jsonPath("$.message").value("Public access is too frequent, please try again later"));
    }

    @Test
    void anonymousPublicDetailRequestsAreThrottledAfterLimit() throws Exception {
        mockMvc.perform(get("/api/public-post/999999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Public post not found"));

        mockMvc.perform(get("/api/public-post/999999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(1006))
            .andExpect(jsonPath("$.message").value("Public access is too frequent, please try again later"));
    }

    @Test
    void anonymousPreviewRequestsAreThrottledAfterLimit() throws Exception {
        mockMvc.perform(get("/api/files/999999/preview"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Image not found"));

        mockMvc.perform(get("/api/files/999999/preview"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(1006))
            .andExpect(jsonPath("$.message").value("Public access is too frequent, please try again later"));
    }
}
