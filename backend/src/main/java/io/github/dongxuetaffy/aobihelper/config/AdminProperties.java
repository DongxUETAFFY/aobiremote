package io.github.dongxuetaffy.aobihelper.config;

import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {
    private Set<String> emails = new HashSet<>(Set.of("admin@aobi.local"));

    public Set<String> getEmails() {
        return emails;
    }

    public void setEmails(Set<String> emails) {
        this.emails = emails == null ? new HashSet<>() : emails;
    }
}

