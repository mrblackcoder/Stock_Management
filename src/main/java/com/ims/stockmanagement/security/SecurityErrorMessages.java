package com.ims.stockmanagement.security;

/**
 * Public-facing security error messages.
 *
 * These are the only strings the API returns for authentication and authorization
 * failures. They are deliberately constant and free of exception classes, token
 * details, stack traces and endpoint internals, so a caller cannot use an error body
 * to learn anything about the server, the token or the resource it asked for.
 */
public final class SecurityErrorMessages {

    /** No credentials at all: the request was never authenticated. */
    public static final String AUTHENTICATION_REQUIRED = "Authentication is required to access this resource.";

    /** Credentials present and understood, but the principal may not have this resource. */
    public static final String ACCESS_FORBIDDEN = "You do not have permission to access this resource.";

    /** Credentials present but not usable, e.g. a token belonging to a disabled account. */
    public static final String AUTHENTICATION_INVALID = "Authentication is invalid.";

    private SecurityErrorMessages() {
    }
}
