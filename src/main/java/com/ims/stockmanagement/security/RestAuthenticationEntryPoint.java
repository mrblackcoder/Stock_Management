package com.ims.stockmanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Entry point for requests that carry no usable authentication.
 *
 * Without it the stateless chain falls back to Spring Security's default entry point,
 * which answers an unauthenticated request with 403. This makes the missing-credentials
 * case a stable JSON 401 instead, and keeps 403 to mean "authenticated but not allowed".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Full detail stays server-side; the caller only learns that credentials are needed.
        log.debug("Unauthenticated request rejected", authException);
        responseWriter.write(response, HttpStatus.UNAUTHORIZED, SecurityErrorMessages.AUTHENTICATION_REQUIRED);
    }
}
