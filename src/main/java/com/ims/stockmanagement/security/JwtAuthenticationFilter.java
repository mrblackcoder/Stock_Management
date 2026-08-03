package com.ims.stockmanagement.security;

import com.ims.stockmanagement.models.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SecurityErrorResponseWriter responseWriter;

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
                Optional<User> loadedUser = userRepository.findByUsername(username);

                if (loadedUser.isPresent()) {
                    User userDetails = loadedUser.get();

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        // Account state is re-checked on every request. A token stays
                        // cryptographically valid until it expires, so without this a user
                        // disabled after login would keep full access with the old token.
                        if (!userDetails.isEnabled()) {
                            SecurityContextHolder.clearContext();
                            log.warn("Rejected access token presented for a disabled account");
                            sendErrorResponse(response, HttpStatus.UNAUTHORIZED,
                                    SecurityErrorMessages.AUTHENTICATION_INVALID);
                            return;
                        }

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
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
        responseWriter.write(response, status, message);
    }
}
