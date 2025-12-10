package com.ims.stockmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ims.stockmanagement.dtos.Response;
import com.ims.stockmanagement.repositories.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepository.findByUsername(username).ifPresent(userDetails -> {
                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                });
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has expired. Please login again.");
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token format: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token format");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token signature");
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token validation failed");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Authentication error occurred");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Response errorResponse = Response.builder()
                .statusCode(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
