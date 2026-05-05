package io.github.dongxuetaffy.aobihelper.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.dongxuetaffy.aobihelper.BackendApplication;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(
    classes = {
        BackendApplication.class,
        ApiAuthGuardIntegrationTest.TestApiController.class
    },
    properties = {
        "spring.datasource.url=jdbc:h2:mem:auth_guard_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-h2.sql",
        "app.auth.cache.type=memory",
        "app.auth.mail-enabled=false"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("local-lite")
class ApiAuthGuardIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserCanAccessWhitelistedHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousUserCanAccessWhitelistedLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousUserCanAccessWhitelistedPublicPageEndpoint() throws Exception {
        mockMvc.perform(get("/api/public-post/page"))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousUserIsRejectedByDefaultOnUnannotatedApiEndpoint() throws Exception {
        mockMvc.perform(get("/api/test-auth-guard/protected-default"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Login required"));
    }

    @RestController
    @RequestMapping("/api/test-auth-guard")
    static class TestApiController {
        @GetMapping("/protected-default")
        public ApiResponse<String> protectedByDefault() {
            return ApiResponse.success("ok");
        }
    }
}
