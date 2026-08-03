package com.ims.stockmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.stockmanagement.dtos.Response;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Writes the JSON error body used by every security rejection that happens inside the
 * filter chain, before any controller advice can run. Shared by the JWT filter, the
 * authentication entry point and the access denied handler so all three emit the same
 * shape as the rest of the API: statusCode, message, timestamp.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public void write(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Response body = Response.builder()
                .statusCode(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
