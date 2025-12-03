import React from 'react';
import { Navigate } from 'react-router-dom';
import ApiService from './ApiService';

export const ProtectedRoute = ({ children }) => {
    const token = ApiService.getToken(); // Bu artık expire kontrolü de yapıyor

    if (!token) {
        // Token yok veya expired
        ApiService.clearAuth();
        return <Navigate to="/login" replace />;
    }

    return children;
};

export const AdminRoute = ({ children }) => {
    const token = ApiService.getToken();
    const role = ApiService.getRole();

    if (!token) {
        ApiService.clearAuth();
        return <Navigate to="/login" replace />;
    }

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

