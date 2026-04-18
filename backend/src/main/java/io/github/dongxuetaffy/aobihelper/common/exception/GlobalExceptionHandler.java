package io.github.dongxuetaffy.aobihelper.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import io.github.dongxuetaffy.aobihelper.common.api.ApiResponse;
import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(exception.getCode(), exception.getMessage()));
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(40000, "Request validation failed"));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLoginException(NotLoginException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(BusinessCode.UNAUTHORIZED, "Login required"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermissionException(NotPermissionException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.fail(BusinessCode.FORBIDDEN, "Permission denied"));
    }

    @ExceptionHandler(SaTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleSaTokenException(SaTokenException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(BusinessCode.UNAUTHORIZED, "Authentication failed"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception) {
        log.error("Unhandled exception caught by global handler", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(50000, "Internal server error"));
    }
}
