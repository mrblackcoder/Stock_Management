import React from 'react';
import { Navigate } from 'react-router-dom';
import ApiService from './ApiService';

/**
 * True when the browser still holds something worth continuing with.
 *
 * getToken() reports a locally expired access token as absent, so testing it alone
 * made the guard tear down the session - refresh token included - before any request
 * could reach the interceptor that knows how to renew it. A stored refresh token is
 * treated purely as permission to let that one-time refresh run; it is never taken
 * as proof of authorization. The backend still decides every individual request, and
 * a refresh that fails clears the session from the interceptor.
 */
const hasResumableSession = () =>
    Boolean(ApiService.getToken() || ApiService.getRefreshToken());

export const ProtectedRoute = ({ children }) => {
    if (!hasResumableSession()) {
        // Nothing left to resume: drop any stale metadata and send the user to login.
        ApiService.clearAuth();
        return <Navigate to="/login" replace />;
    }

    return children;
};

export const AdminRoute = ({ children }) => {
    const role = ApiService.getRole();

    if (!hasResumableSession()) {
        ApiService.clearAuth();
        return <Navigate to="/login" replace />;
    }

    // Role enforcement is unchanged: an expired access token never widens access.
    if (role !== 'ADMIN') {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

export const PublicRoute = ({ children }) => {
    const token = ApiService.getToken();

    if (token) {
        return <Navigate to="/dashboard" replace />;
    }

    return children;
};

