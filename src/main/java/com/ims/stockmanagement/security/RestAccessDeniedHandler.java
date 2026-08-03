package com.ims.stockmanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles filter-chain authorization failures for callers that did authenticate.
 * Emits the same JSON 403 contract as the controller advice, so a denial reported by
 * the chain and one reported by method security are indistinguishable to a caller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter responseWriter;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        log.debug("Authenticated request denied by the security filter chain", accessDeniedException);
        responseWriter.write(response, HttpStatus.FORBIDDEN, SecurityErrorMessages.ACCESS_FORBIDDEN);
    }
}
