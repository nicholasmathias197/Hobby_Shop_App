package com.hobby.shop.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private WebRequest request;

    @BeforeEach
    void setUp() {
        request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handlesResourceNotFound() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("Missing"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Missing");
    }

    @Test
    void handlesBadRequest() {
        var response = handler.handleBadRequestException(new BadRequestException("Invalid"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
    }

    @Test
    void handlesBadCredentials() {
        var response = handler.handleBadCredentialsException(new BadCredentialsException("bad creds"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid email or password");
    }

    @Test
    void handlesAuthorizationAndAccessDenied() {
        AuthorizationResult authorizationResult = mock(AuthorizationResult.class);
        var authzResponse = handler.handleAuthorizationDeniedException(
            new AuthorizationDeniedException("denied", authorizationResult),
                request
        );
        var accessResponse = handler.handleAccessDeniedException(new AccessDeniedException("nope"), request);

        assertThat(authzResponse.getStatusCode().value()).isEqualTo(403);
        assertThat(authzResponse.getBody().getMessage()).contains("don't have permission");
        assertThat(accessResponse.getStatusCode().value()).isEqualTo(403);
        assertThat(accessResponse.getBody().getMessage()).isEqualTo("Access denied: nope");
    }

    @Test
    void handlesAuthenticationException() {
        var response = handler.handleAuthenticationException(new InsufficientAuthenticationException("missing"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
    }

    @Test
    void handlesValidationExceptions() throws Exception {
        Method method = DummyValidationTarget.class.getDeclaredMethod("handle", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new DummyValidationTarget(), "dummy");
        bindingResult.addError(new FieldError("dummy", "email", "Email is required"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidationExceptions(exception, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getDetails()).containsExactly("Email is required");
    }

    @Test
    void handlesUnexpectedExceptions() {
        var response = handler.handleGlobalException(new IllegalStateException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    private static class DummyValidationTarget {
        @SuppressWarnings("unused")
        void handle(String email) {
        }
    }
}