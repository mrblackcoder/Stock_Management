import React, { useEffect, useState } from 'react';
import ApiService from '../service/ApiService';
import './ProfilePage.css';

function ProfilePage() {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Get user from ApiService
        const userData = ApiService.getUser();
        if (userData) {
            setUser(userData);
        }
        setLoading(false);
    }, []);

    const handleLogout = () => {
        ApiService.clearAuth();
        window.location.href = '/login';
    };

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    return (
        <div className="profile-page">
            <div className="profile-container">
                <div className="profile-header">
                    <div className="profile-avatar">
                        {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
                    </div>
                    <h1>Profile</h1>
                </div>

                <div className="profile-info">
                    <div className="info-row">
                        <label>Full Name:</label>
                        <span>{user?.fullName || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                        <label>Username:</label>
                        <span>{user?.username || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                        <label>Email:</label>
                        <span>{user?.email || 'N/A'}</span>
                    </div>
                    <div className="info-row">
                        <label>Role:</label>
                        <span className="role-badge">{user?.role || 'USER'}</span>
                    </div>
                </div>

                <div className="profile-actions">
                    <button className="btn-primary" onClick={() => window.location.href = '/dashboard'}>
                        Back to Dashboard
                    </button>
                    <button className="btn-danger" onClick={handleLogout}>
                        Logout
                    </button>
                </div>
            </div>
        </div>
    );
}

export default ProfilePage;

