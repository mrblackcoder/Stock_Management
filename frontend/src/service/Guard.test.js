import React from 'react';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { AdminRoute, ProtectedRoute } from './Guard';
import ApiService from './ApiService';

/**
 * Exercises the real guard components and the real routing decision; only the
 * ApiService token getters are stubbed.
 *
 * The behaviour under test is the session-teardown rule: a locally expired access
 * token used to make the guard call clearAuth(), which deleted the refresh token
 * before any request could reach the refresh interceptor.
 */
jest.mock('./ApiService', () => ({
    __esModule: true,
    default: {
        getToken: jest.fn(),
        getRefreshToken: jest.fn(),
        getRole: jest.fn(),
        clearAuth: jest.fn(),
        refreshToken: jest.fn()
    }
}));

const PROTECTED_CONTENT = 'protected content';
const ADMIN_CONTENT = 'admin content';
const LOGIN_CONTENT = 'login page';
const DASHBOARD_CONTENT = 'dashboard page';

const renderGuardedRoute = (path, element) => render(
    <MemoryRouter initialEntries={[path]}>
        <Routes>
            <Route path={path} element={element} />
            <Route path="/login" element={<div>{LOGIN_CONTENT}</div>} />
            <Route path="/dashboard" element={<div>{DASHBOARD_CONTENT}</div>} />
        </Routes>
    </MemoryRouter>
);

const renderProtected = () => renderGuardedRoute(
    '/products',
    <ProtectedRoute><div>{PROTECTED_CONTENT}</div></ProtectedRoute>
);

const renderAdmin = () => renderGuardedRoute(
    '/admin',
    <AdminRoute><div>{ADMIN_CONTENT}</div></AdminRoute>
);

describe('ProtectedRoute', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('validAccessTokenRendersTheProtectedRoute', () => {
        ApiService.getToken.mockReturnValue('valid-access-token');
        ApiService.getRefreshToken.mockReturnValue('stored-refresh-token');

        renderProtected();

        expect(screen.getByText(PROTECTED_CONTENT)).toBeInTheDocument();
        expect(screen.queryByText(LOGIN_CONTENT)).not.toBeInTheDocument();
        expect(ApiService.clearAuth).not.toHaveBeenCalled();
    });

    test('expiredAccessTokenWithRefreshTokenPreservesSession', () => {
        // getToken() reports a locally expired access token as absent.
        ApiService.getToken.mockReturnValue(null);
        ApiService.getRefreshToken.mockReturnValue('stored-refresh-token');

        renderProtected();

        // The route renders, so its first API call can drive the one-time refresh.
        expect(screen.getByText(PROTECTED_CONTENT)).toBeInTheDocument();
        expect(screen.queryByText(LOGIN_CONTENT)).not.toBeInTheDocument();

        expect(ApiService.clearAuth).not.toHaveBeenCalled();
        expect(ApiService.getRefreshToken()).toBe('stored-refresh-token');

        // The guard never renews credentials itself.
        expect(ApiService.refreshToken).not.toHaveBeenCalled();
    });

    test('missingAccessAndRefreshTokensRedirectsToLogin', () => {
        ApiService.getToken.mockReturnValue(null);
        ApiService.getRefreshToken.mockReturnValue(null);

        renderProtected();

        expect(screen.queryByText(PROTECTED_CONTENT)).not.toBeInTheDocument();
        expect(screen.getByText(LOGIN_CONTENT)).toBeInTheDocument();
        expect(ApiService.clearAuth).toHaveBeenCalled();
        expect(ApiService.refreshToken).not.toHaveBeenCalled();
    });
});

describe('AdminRoute', () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('adminRouteWithRefreshSessionPreservesRoleEnforcement', () => {
        // A refresh-only session keeps an ADMIN on the admin route...
        ApiService.getToken.mockReturnValue(null);
        ApiService.getRefreshToken.mockReturnValue('stored-refresh-token');
        ApiService.getRole.mockReturnValue('ADMIN');

        const admin = renderAdmin();

        expect(screen.getByText(ADMIN_CONTENT)).toBeInTheDocument();
        expect(ApiService.clearAuth).not.toHaveBeenCalled();
        admin.unmount();

        // ...and still refuses a USER, so an expired access token never widens access.
        jest.clearAllMocks();
        ApiService.getToken.mockReturnValue(null);
        ApiService.getRefreshToken.mockReturnValue('stored-refresh-token');
        ApiService.getRole.mockReturnValue('USER');

        renderAdmin();

        expect(screen.queryByText(ADMIN_CONTENT)).not.toBeInTheDocument();
        expect(screen.getByText(DASHBOARD_CONTENT)).toBeInTheDocument();
        // Denied for role, not signed out: the refresh session survives.
        expect(ApiService.clearAuth).not.toHaveBeenCalled();
        expect(ApiService.getRefreshToken()).toBe('stored-refresh-token');
    });

    test('adminRouteWithoutAnySessionRedirectsToLogin', () => {
        ApiService.getToken.mockReturnValue(null);
        ApiService.getRefreshToken.mockReturnValue(null);
        ApiService.getRole.mockReturnValue('ADMIN');

        renderAdmin();

        expect(screen.queryByText(ADMIN_CONTENT)).not.toBeInTheDocument();
        expect(screen.getByText(LOGIN_CONTENT)).toBeInTheDocument();
        expect(ApiService.clearAuth).toHaveBeenCalled();
    });
});
