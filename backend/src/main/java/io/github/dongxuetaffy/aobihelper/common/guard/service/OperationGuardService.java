package io.github.dongxuetaffy.aobihelper.common.guard.service;

public interface OperationGuardService {
    void assertMutationAllowed(Long userId, String action, String requestId);
}
